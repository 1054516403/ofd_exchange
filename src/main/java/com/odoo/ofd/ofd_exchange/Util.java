package com.odoo.ofd.ofd_exchange;

import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Util {

    public String encodeBase64FromFilePath(String path) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[1024];
        while((len = fis.read(buffer)) != -1) {
            bao.write(buffer, 0, len);
        }
        byte[] data = null;
        data = bao.toByteArray();
        fis.close();
        bao.close();
        return Base64.getEncoder().encodeToString(data);
    }

    public void deleteDir(File directory){
        //获取目录下所有文件和目录
        File files[] = directory.listFiles();
        for (File file : files) {
            if(file.isDirectory()){
                deleteDir(file);
            }else {
                file.delete();
                System.out.println(file.getName()+"：：文件已删除");
            }
        }
        //最终把该目录也删除
        directory.delete();
        System.out.println(directory.getName()+"：：目录已删除");
    }
}
