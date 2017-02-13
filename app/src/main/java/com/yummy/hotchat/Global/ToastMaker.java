package com.yummy.hotchat.Global;

import android.content.Context;
import android.widget.Toast;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/12 17:48
 * 修改人：qq985
 * 修改时间：2016/12/12 17:48
 * 修改备注：
 */
public class ToastMaker {

    public static void toastSomething(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
