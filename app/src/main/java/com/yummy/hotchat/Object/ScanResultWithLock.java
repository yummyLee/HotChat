package com.yummy.hotchat.Object;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/22 22:49
 * 修改人：qq985
 * 修改时间：2016/12/22 22:49
 * 修改备注：
 */
public class ScanResultWithLock {

    ScanResult scanResult;
    boolean isLocked;

    public ScanResultWithLock(ScanResult scanResult, boolean isLocked) {
        this.scanResult = scanResult;
        this.isLocked = isLocked;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
