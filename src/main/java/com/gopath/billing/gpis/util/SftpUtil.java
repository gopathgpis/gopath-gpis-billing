package com.gopath.billing.gpis.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.codec.Base64;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;


@Slf4j
@Service
public class SftpUtil {

    public static void main(String[] args) {
//        String loginName = "chs25_gpis";
//        String loginPassword = "dotyast7";
//        String server = "63.114.146.30";
//        Integer port = 22;
        SftpUtil sftpUtil = new SftpUtil();
        boolean flag = sftpUtil.checkFileExists("ginorth01","test.gopathdigital.com",22,"GpSfGiNorD@#$","/results","UPDATE_GINORTH_20240906074220579.hl7");
        System.out.println("flag:"+flag);
        //上传文件
        //sftpUtil.uploadFile(loginName,server,port,loginPassword,"F:\\sfpt\\上传至sftp服务器.txt","从sftp服务器上下载.txt");
        //下载文件
        //sftpUtil.downloadFile("ceshi","121.5.105.81",6008,"gopath","GPIS_bill_20221008142345.hl7","F:\\sfpt\\GPIS_bill_20221008142345.hl7");
        //写文件
        //sftpUtil.writeFile();
        //读文件
        //sftpUtil.readFile();
        //删除文件
        //sftpUtil.deleteFile(loginName, server, port,loginPassword);
        //sftpUtil.writeFile(loginName,server,port,loginPassword,"./to_arkstone","test.json","test sdsds");
        /*String str = sftpUtil.readFile(loginName,server,port,loginPassword,"./from_arkstone","OutUTI220424000029.json");
        JSONObject reportObj = JSONObject.parseObject(str);
        String base64Content = reportObj.getString("report_content");
        String filePath= "D:\\upload\\ixlayer\\pdf\\"+new Date().getTime()+".pdf";
        base64StringToPdf(base64Content,filePath);*/

//        String str = PDFToBase64(new File("F:\\公司文件夹\\gopath-病理\\分子sftp\\Uropartner\\GM22-2503 ProstateNow Report.pdf"));
//        base64StringToPdf(str,"F:\\公司文件夹\\gopath-病理\\分子sftp\\Uropartner\\testSave.pdf");
    }

    /**
     * 连接登陆远程服务器
     *
     * @return
     */
    public static ChannelSftp connect(String loginName, String server,Integer port,String loginPassword)  {
        JSch jSch = new JSch();
        Session session = null;
        ChannelSftp sftp = null;

        log.info("Connect: server = {}, port = {}, loginName = {}, password = {}", server, port, loginName, loginPassword);

        try {
            session = jSch.getSession(loginName, server, port);
            session.setPassword(loginPassword);
            session.setConfig(getSshConfig());
            session.connect();

            sftp = (ChannelSftp)session.openChannel("sftp");
            sftp.connect();
        } catch (Exception e) {
            log.error("SSH connecting to FTP server: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }

        return sftp;
    }

    /**
     * 获取服务配置
     * @return
     */
    private static Properties getSshConfig() {
        Properties sshConfig =  new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        return sshConfig;
    }


    /**
     * 关闭连接
     * @param sftp
     */
    public static void disconnect(ChannelSftp sftp) {
        try {
            if(sftp!=null){
                if(sftp.getSession().isConnected()){
                    sftp.getSession().disconnect();
                }
            }
        } catch (Exception e) {
            log.error("关闭与sftp服务器会话连接异常",e);
        }
    }


    /**
     * 下载远程sftp服务器文件
     * @param remoteFilename "downfile.txt"
     * @param LocalFile "F:\\sfpt\\downfile.txt"
     */
    public void downloadFile(String loginName, String server,Integer port,String loginPassword,String remoteFilename,String LocalFile) {
        FileOutputStream output = null;
        ChannelSftp sftp = null;
        try {
            sftp = connect(loginName,server,port,loginPassword);
            if(sftp == null){
                return ;
            }
            //sftp服务器上文件路径
            //String remoteFilename = "downfile.txt";
            //下载至本地路径
            File localFile = new File(LocalFile);
            output = new FileOutputStream(localFile);

            sftp.get(remoteFilename, output);
            System.out.println("成功接收文件,本地路径：" + localFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("接收文件异常!",e);
        } finally {
            try {
                if (null != output) {
                    output.flush();
                    output.close();
                }
                // 关闭连接
                disconnect(sftp);
            } catch (IOException e) {
                log.error("关闭文件时出错!",e);
            }
        }
    }


    /**
     * 读取远程sftp服务器文件
     * @param loginName
     * @param server
     * @param port
     * @param loginPassword
     * @param remotePath
     * @param remoteFilename
     */
    public String readFile(String loginName, String server,Integer port,String loginPassword,String remotePath,String remoteFilename) {
        InputStream in = null;
        ArrayList<String> strings = new ArrayList<>();
        ChannelSftp sftp = null;
        String readStr = "";
        String str = "";
        try {
            sftp = connect(loginName,server,port,loginPassword);
            if(sftp == null){
                return str;
            }
//            String remotePath = "./";
//            String remoteFilename = "writeFile.txt";
            sftp.cd(remotePath);
            if(!listFiles(loginName,server,port,loginPassword,remotePath).contains(remoteFilename)){
                log.error("no such file");
                return str;
            }
            in = sftp.get(remoteFilename);
            if (in != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in,"utf-8"));
                while ((str = br.readLine()) != null) {
                    if(StringUtils.isNotEmpty(str)){
                        readStr += str+"\r";
                    }
                }
            }else{
                log.error("in为空，不能读取。");
            }
        } catch (Exception e) {
            log.error("接收文件时异常!",e);
        }  finally {
            try {
                if(in !=null){
                    in.close();
                }
                // 关闭连接
                disconnect(sftp);
            } catch (Exception e) {
                log.error("关闭文件流时出现异常!",e);
            }
        }
        return readStr;
    }


    /**
     * 写文件至远程sftp服务器
     * @param loginName
     * @param server
     * @param port
     * @param loginPassword
     * @param remotePath "./"
     * @param remoteFilename "writeFile.txt";
     * @param content "测试内容";
     */
    public void writeFile(String loginName, String server,Integer port,String loginPassword,String remotePath,String remoteFilename,String content){
        ChannelSftp sftp = null;
        ByteArrayInputStream input = null;
        try {
            sftp = connect(loginName,server,port,loginPassword);
            if(sftp == null){
                return;
            }
            // 更改服务器目录
//            String remotePath = "./";
            sftp.cd(remotePath);
            // 发送文件
//            String remoteFilename = "writeFile.txt";
//            String content = "测试内容";
            input = new ByteArrayInputStream(content.getBytes());
            sftp.put(input, remoteFilename);
        } catch (Exception e) {
            log.error("发送文件时有异常!",e);
        } finally {
            try {
                if (null != input) {
                    input.close();
                }
                // 关闭连接
                disconnect(sftp);
            } catch (Exception e) {
                log.error("关闭文件时出错!",e);
            }
        }
    }


    /**
     * 上传文件至sftp服务器
     * @param localPath "F:\\sfpt\\上传至sftp服务器.txt"
     * @param remoteFilename "从sftp服务器上下载.txt"
     */
    public static String uploadFile(String loginName, String server,Integer port,String loginPassword, String localPath,String remoteFilename) {
        String status = "";
        FileInputStream fis = null;
        ChannelSftp sftp = null;
        // 上传文件至服务器此目录
        /*String localPath = "F:\\sfpt\\上传至sftp服务器.txt";
        String remoteFilename = "从sftp服务器上下载.txt";*/
        try {
            sftp = connect(loginName,server,port,loginPassword);
            if(sftp == null){
                status = "Collect sftp Fail";
                return status;
            }

            File localFile = new File(localPath);
            fis = new FileInputStream(localFile);
            //发送文件
            sftp.put(fis, remoteFilename);
            status = "Success";
            log.info("File sftp successful: {}", remoteFilename);
        } catch (Exception e) {
            status = "Exception Failure";
            log.error("Error in file sftp: {}", e.getMessage());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                // 关闭连接
                disconnect(sftp);
            } catch (Exception e) {
                status = "Close Failure";
                log.error("Error closing file: {}", e.getMessage());
            }
        }
        return status;
    }


    /**
     * 遍历远程文件
     *
     * @param remotePath
     * @return
     * @throws Exception
     */
    public List<String> listFiles(String loginName, String server,Integer port,String loginPassword,String remotePath){
        List<String> ftpFileNameList = new ArrayList<String>();
        ChannelSftp.LsEntry isEntity = null;
        String fileName = null;
        ChannelSftp sftp = null;
        try{
            sftp = connect(loginName,server,port,loginPassword);
            if(sftp == null){
                return null;
            }
            Vector<ChannelSftp.LsEntry> sftpFile = sftp.ls(remotePath);
            for (ChannelSftp.LsEntry entry : sftpFile) {
                fileName = entry.getFilename();

                // 排除 . 和 ..
                if (!".".equals(fileName) && !"..".equals(fileName)) {
                    ftpFileNameList.add(fileName);
                }
            }

            return ftpFileNameList;
        }catch (Exception e){
            log.error("遍历查询sftp服务器上文件异常",e);
            return null;
        }finally {
            disconnect(sftp);
        }

    }


    public boolean checkFileExists(String loginName, String server,Integer port,String loginPassword,String remotePath, String fileName){
        ChannelSftp.LsEntry isEntity = null;
        ChannelSftp sftp = null;
        boolean existFlag = false;
        try{
            sftp = connect(loginName,server,port,loginPassword);
            if(sftp == null){
                return existFlag;
            }
            // 如果 remotePath 未传，使用当前目录
            if (remotePath == null || remotePath.isEmpty()) {
                remotePath = sftp.pwd();  // 获取当前 SFTP 工作目录
            }
            // 直接检查文件是否存在
            try {
                sftp.ls(remotePath + "/" + fileName);  // 如果文件存在不会抛异常
                existFlag = true;
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    existFlag = false;  // 文件不存在
                } else {
                    throw e;  // 其他异常继续抛出
                }
            }
            return existFlag;
        }catch (Exception e){
            log.error("遍历查询sftp服务器上文件异常",e);
            return existFlag;
        }finally {
            disconnect(sftp);
        }

    }


    /**
     * 删除远程文件
     * @param loginName
     * @param server
     * @param port
     * @param loginPassword
     * @param remotePath "./"
     * @param remoteFilename "limit.txt"
     */
    public void deleteFile(String loginName, String server,Integer port,String loginPassword,String remotePath,String remoteFilename) {
        boolean success = false;
        ChannelSftp sftp = null;
        try {
            sftp = connect(loginName,server,port,loginPassword);
            if(sftp == null){
                return;
            }
//            String remotePath = "./";
//            String remoteFilename = "limit.txt";
            // 更改服务器目录
            sftp.cd(remotePath);
            //判断文件是否存在
            if(listFiles(loginName,server,port,loginPassword,remotePath).contains(remoteFilename)){
                // 删除文件
                sftp.rm(remoteFilename);
                log.info("删除远程文件" + remoteFilename + "成功!");
            }

        } catch (Exception e) {
            log.error("删除文件时有异常!",e);
        } finally {
            // 关闭连接
            disconnect(sftp);
        }
    }


    /**
     * Description: 将base64编码内容转换为Pdf * @param base64编码内容，文件的存储路径（含文件名）
     * @param base64Content
     * @param filePath
     */
    public static void base64StringToPdf(String base64Content,String filePath){
//        BASE64Decoder decoder = new BASE64Decoder();
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
//            byte[] bytes = decoder.decodeBuffer(base64Content);//base64编码内容转换为字节数组
            byte[] bytes = Base64.decode(base64Content);
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
            bis = new BufferedInputStream(byteInputStream);
            File file = new File(filePath);
            File path = file.getParentFile();
            if(!path.exists()){
                path.mkdirs();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            byte[] buffer = new byte[1024];
            int length = bis.read(buffer);
            while(length != -1){
                bos.write(buffer, 0, length);
                length = bis.read(buffer);
            }
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(bis != null){
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Description: 将pdf文件转换为Base64编码 * @param 要转的的pdf文件
     * @param file
     * @return
     */
    public static String PDFToBase64(File file) {
        FileInputStream fin =null;
        BufferedInputStream bin =null;
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout =null;
        try {
            fin = new FileInputStream(file);
            bin = new BufferedInputStream(fin);
            baos = new ByteArrayOutputStream();
            bout = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024];
            int len = bin.read(buffer);
            while(len != -1){
                bout.write(buffer, 0, len);
                len = bin.read(buffer);
            }
            //刷新此输出流并强制写出全部缓冲的输出字节
            bout.flush();
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                if(fin != null){
                    fin.close();
                }
                if(bin != null){
                    bin.close();
                }
                if(bout != null){
                    bout.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

