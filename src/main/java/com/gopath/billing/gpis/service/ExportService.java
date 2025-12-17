package com.gopath.billing.gpis.service;

import com.gopath.billing.gpis.dao.GpisBillingDao;
import com.gopath.billing.gpis.dto.SftpRecordDto;
import com.gopath.billing.gpis.entity.SftpExport;
import com.gopath.billing.gpis.entity.SysEnumData;
import com.gopath.billing.gpis.util.SftpUtil;
import com.gopath.billing.gpis.util.WriteFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.gopath.domain.persistent.*;
import static com.gopath.billing.gpis.util.TimeZoneUtils.formateByInstant;


@Service
@Slf4j(topic = "c.g.b.g.service.ExportService")
public class ExportService {

    @Value("${domain.backend}")
    private String backendDomain;

    @Value("${prop.upload-folder}")
    private String uploadFolder;

    @Value("${billing.uti.sftp-kareo}")
    private String sftpKareo;

    @Autowired
    private WriteFileUtil writeFileUtil;

    @Autowired
    private GpisBillingDao gpisBillingDao;

    public void exporHl7Uti(UtiCase utiCase, String hl7String, String timeZone/*, String finalStatus*/) throws Exception {
        log.info("Writing HL7 to disk and sftp: {}", hl7String);
        SftpRecordDto srd = new SftpRecordDto();

        String filePrefix = formateByInstant(new Date().getTime(), timeZone, "yyyyMMddHHmmss");
        String fileName = "GPIS_bill_" + utiCase.getGopathId() + "_" + filePrefix + ".hl7";
        String hl7Path = "kareo/" + fileName;
        String httpPath = "";
        String status = writeFileUtil.writerBuffer(new StringBuffer(hl7String), hl7Path);

        srd.setFileName(fileName);
        srd.setLocalPath(uploadFolder + hl7Path);
        srd.setCsvStatus(status);

        // https://gopathdigital.com/basic-api/upload/kareo/GPIS_bill_GM22-0049_20230503202509.hl7
        httpPath = backendDomain + "/upload/" + hl7Path;
        srd.setHttpCsv(httpPath);

        Map<String, String> kareoConfig = returnKareoConfig();

        CompletableFuture.supplyAsync(() -> {
            try {
                sendKareoTask(kareoConfig, srd, utiCase);
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error: sending Kareo: {}", utiCase.getId());
                return "ERROR" ;
            }
        });

    }

    public Map<String, String> returnKareoConfig() throws Exception {
        List<SysEnumData> sysEnumDataList = gpisBillingDao.loadEnumDataByEnumCode("kareoSftp");

        Map<String, String> returnMap = new HashMap<>();

        String loginName = "";
        String server = "";
        String port = "";
        String loginPassword = "";

        if (sysEnumDataList != null && !sysEnumDataList.isEmpty()) {
            for (SysEnumData sysEnumData : sysEnumDataList) {
                if (StringUtils.equals(sysEnumData.getSysEnumDataValue(), "username")) {
                    loginName = sysEnumData.getSysEnumDataKey();
                } else if (StringUtils.equals(sysEnumData.getSysEnumDataValue(), "hostname")) {
                    server = sysEnumData.getSysEnumDataKey();
                } else if (StringUtils.equals(sysEnumData.getSysEnumDataValue(), "port")) {
                    port = sysEnumData.getSysEnumDataKey();
                } else if (StringUtils.equals(sysEnumData.getSysEnumDataValue(), "password")) {
                    loginPassword = sysEnumData.getSysEnumDataKey();
                }
            }
        }

        log.info("Kareo config: {} {} {} {}", server, port, loginName, loginPassword);

        returnMap.put("loginName", loginName);
        returnMap.put("server", server);
        returnMap.put("port", port);
        returnMap.put("loginPassword", loginPassword);

        return returnMap;
    }

    public void sendKareoTask(Map<String, String> kareoConfig, SftpRecordDto srd, UtiCase utiCase) throws Exception {
        log.info("Sending to Kareo: {}", srd.getFileName());

        String status = !"true".equals(sftpKareo) ? "sftp step skipped" : SftpUtil.uploadFile(
                kareoConfig.get("loginName"),
                kareoConfig.get("server"),
                Integer.parseInt(kareoConfig.get("port")),
                kareoConfig.get("loginPassword"),
                srd.getLocalPath(),
                srd.getFileName()
        );

        if (StringUtils.equals(status, "Success")) {
            gpisBillingDao.updateUtiCaseAndBillingDetail("1", utiCase.getId());
        }

        gpisBillingDao.saveSftpExportRecord(
                new SftpExport(
                        srd.getFileName(),
                        srd.getHttpCsv(),
                        srd.getLocalPath(),
                        kareoConfig.get("server"),
                        1,
                        srd.getCsvStatus(), // status of writing HL7 to local file
                        "uti-kareo",
                        ZoneId.systemDefault().getId(),
                        status, // status of sending sftp in err_msg column
                        "auto",
                        "auto",
                        utiCase.getId(),
                        "uti")
        );

    }
}

