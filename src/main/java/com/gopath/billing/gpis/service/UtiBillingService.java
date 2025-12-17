package com.gopath.billing.gpis.service;

import cn.hutool.core.util.StrUtil;
import com.gopath.billing.gpis.dao.GpisBillingDao;

import com.gopath.domain.constant.GoPathEnum;
import com.gopath.domain.persistent.*;
import com.gopath.uti.portal.model.UtiPortalCustomer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;


@Service
@Slf4j(topic = "c.g.b.g.service.GpisBillingService")
public class UtiBillingService {

    @Value("${billing.uti.auto-send-kareo}")
    String autoSendKareo;

    @Autowired
    GpisBillingDao gpisBillingDao;

    @Autowired
    Hl7Service hl7Service;

    @Autowired
    ExportService exportService;

    public String doBillingUti (String utiPortalOrderId) throws Exception {
        UtiCase utiCase = gpisBillingDao.findByUtiPortalOrderId(utiPortalOrderId);

        log.info("Found UTI case with the given uti_portal_order_id: {}", utiPortalOrderId);

        BillingDetail billingDetail = gpisBillingDao.createOrUpdateBillingDetailRecord(utiCase);

        log.info("Auto send Kareo: {}", autoSendKareo);

        if ("true".equals(autoSendKareo)) {
            log.info("Sending Kareo");
            String hl7String = hl7Service.getHL7String(utiCase, billingDetail, "America/Chicago");
            this.exportBilling(utiPortalOrderId, hl7String);
        }

        return "ok";
    }

    public String exportBilling(String utiPortalOrderId) throws Exception {
        this.exportBilling(utiPortalOrderId, null);
        return "ok";
    }

    public void exportBilling(String utiPortalOrderId, String hl7String) throws Exception {
        UtiCase utiCase = gpisBillingDao.findByUtiPortalOrderId(utiPortalOrderId);

        if (hl7String == null) {
            BillingDetail billingDetail = gpisBillingDao.getBillingDetailByCaseId(utiCase.getId());
            hl7String = hl7Service.getHL7String(utiCase, billingDetail, "America/Chicago");
        }

        exportService.exporHl7Uti(utiCase, hl7String, "America/Chicago");
    }



}