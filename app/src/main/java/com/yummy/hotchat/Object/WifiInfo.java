package com.yummy.hotchat.Object;

import android.net.wifi.ScanResult;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/11/27 13:37
 * 修改人：qq985
 * 修改时间：2016/11/27 13:37
 * 修改备注：
 */
public class WifiInfo {

    private String ip;
    private String ssid;
    private String password;

    private int resId;
    private int rri;

    private ScanResult scanResult;

    public WifiInfo(ScanResult scanResult) {

        this.scanResult = scanResult;

    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getRri() {
        return rri;
    }

    public void setRri(int rri) {
        this.rri = rri;
    }
}
