package com.gopath.billing.gpis.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


import java.io.Serializable;

@Data
@Entity
@Table(name="sftp_export")
@NoArgsConstructor
@Accessors(chain=true)
public class SftpExport extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "sftp_file_name", length = 400, nullable = true)
    private String sftpFileName; // 文件名

    /**
     * caseid
     */
    @Column(name = "business_id", length = 40, nullable = true)
    private String businessId = "";

    /**
     *
     */
    @Column(name = "business_type", length = 20, nullable = true)
    private String businessType; // 业务类型

    /**
     * http路径
     */
    @Column(name = "http_csv", length = 400, nullable = true)
    private String httpCsv;

    /**
     * 服务器物理路径
     */
    @Column(name = "local_path", length = 400, nullable = true)
    private String localPath;

    /**
     * sftp服务器
     */
    @Column(name = "sftp_service", length = 100, nullable = true)
    private String sftpService; // sftp服务器

    /**
     * 上传数目
     */
    @Column(name = "export_num",  nullable = true)
    private Integer exportNum; // 上传数目

    /**
     * 发送状态
     */
    @Column(name = "status", length = 100, nullable = true)
    private String status; // 发送状态

    /**
     * 文件类型
     */
    @Column(name = "sftp_type", length = 40, nullable = true)
    private String sftpType; // 文件类型

    /**
     * 时区
     */
    @Column(name = "time_zone", length = 100, nullable = true)
    private String timeZone;

    @Column(name = "error_msg", length = 500, nullable = true)
    private String errorMsg;

    /**
     * 发送人
     */
    @Column(name = "send_user_id", length = 50, nullable = true)
    private String sendUserId;

    @Column(name = "send_user", length = 200, nullable = true)
    private String sendUser;

    public SftpExport(String sftpFileName, String httpCsv, String localPath, String sftpService, Integer exportNum, String status, String sftpType,
                      String timeZone,String errorMsg, String sendUserId,String sendUser,String businessId, String businessType) {
        this.sftpFileName = sftpFileName;
        this.httpCsv = httpCsv;
        this.localPath = localPath;
        this.sftpService = sftpService;
        this.exportNum = exportNum;
        this.status = status;
        this.sftpType = sftpType;
        this.timeZone = timeZone;
        this.errorMsg = errorMsg;
        this.sendUser = sendUser;
        this.sendUserId = sendUserId;
        this.businessId = businessId;
        this.businessType = businessType;
    }
}

