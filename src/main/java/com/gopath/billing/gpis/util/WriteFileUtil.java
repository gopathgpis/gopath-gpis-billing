package com.gopath.billing.gpis.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class WriteFileUtil {

    @Value("${prop.upload-folder}")
    private String uploadFolder;

    public String writerBuffer(StringBuffer buffer,String filePath){
        String csvStatus = "Success";

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(
                        new File(uploadFolder + filePath)))))         {
            bw.write(buffer.toString());
            bw.flush();
        } catch (IOException e) {
            csvStatus = "Fail";
        }

        return csvStatus;
    }
}
