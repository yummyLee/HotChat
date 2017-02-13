package com.yummy.hotchat.Object;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/17 0:34
 * 修改人：qq985
 * 修改时间：2016/12/17 0:34
 * 修改备注：
 */
public class FileInfo {

    String fileName;
    String fileSize;
    String filePath;
    String fileTime;
    long lastModified;

    public FileInfo(String fileName, String fileSize, String filePath, String fileTime) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.fileTime = fileTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileTime() {
        return fileTime;
    }

    public void setFileTime(String fileTime) {
        this.fileTime = fileTime;
    }

    @Override
    public String toString() {
        return "文件名：" + fileName + "，文件大小：" + fileSize + "，文件时间：" + fileTime;
    }

    public static String switchTime(long time) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);//System.currentTimeMillis()替换成long lastModified
        return df.format(date.getTime());
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}

