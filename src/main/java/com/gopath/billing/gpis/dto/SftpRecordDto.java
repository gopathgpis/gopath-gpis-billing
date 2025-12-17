package com.gopath.billing.gpis.dto;

import lombok.Data;


@Data
public class SftpRecordDto {

    private String fileName;  //文件名称

    private String httpCsv; //http路径

    private String localPath; //物理路径

    /**
     * success / successButPersonal / successButSignUser / successButSignOrgan
     */
    private String csvStatus; //写入csv状态

    private String errorMessage;

    private Integer csvNum; //导出数目
}
