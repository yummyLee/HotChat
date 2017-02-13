package com.yummy.hotchat.WifiTool;

import android.net.wifi.ScanResult;

import java.util.Comparator;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/12 13:32
 * 修改人：qq985
 * 修改时间：2016/12/12 13:32
 * 修改备注：
 */
public class SortResultsByLevel implements Comparator {

    @Override
    public int compare(Object lhs, Object rhs) {
        ScanResult s1 = (ScanResult) lhs;
        ScanResult s2 = (ScanResult) rhs;
        if (s1.level < s2.level) {
            return 1;
        } else if (s1.level == s2.level) {
            return 0;
        } else {
            return -1;
        }
    }
}
