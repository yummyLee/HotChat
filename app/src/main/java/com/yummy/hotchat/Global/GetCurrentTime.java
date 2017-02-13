package com.yummy.hotchat.Global;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/23 1:42
 * 修改人：qq985
 * 修改时间：2016/12/23 1:42
 * 修改备注：
 */
public class GetCurrentTime {

    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        return str;
    }
}
