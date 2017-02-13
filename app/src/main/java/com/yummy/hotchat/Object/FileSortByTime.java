package com.yummy.hotchat.Object;

import java.util.Comparator;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/25 22:18
 * 修改人：qq985
 * 修改时间：2016/12/25 22:18
 * 修改备注：
 */
public class FileSortByTime implements Comparator {

    @Override
    public int compare(Object lhs, Object rhs) {
        FileInfo s1 = (FileInfo) lhs;
        FileInfo s2 = (FileInfo) rhs;
        if (s1.getLastModified() < s2.getLastModified()) {
            return 1;
        } else if (s1.getLastModified() == s2.getLastModified()) {
            return 0;
        } else {
            return -1;
        }
    }
}
