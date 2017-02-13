package com.yummy.hotchat.Object;

import android.util.Log;

import com.yummy.hotchat.Socket.SocketClient;
import com.yummy.hotchat.Socket.SocketServer;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/12/14 12:51
 * 修改人：qq985
 * 修改时间：2016/12/14 12:51
 * 修改备注：
 */
public class SocketUser {

    private String userName;
    private String userIllustration;
    private String isMan;
    private String icon;
    private String time;
    private int sign;

    public SocketUser(String userName, String userIllustration, String isMan, String icon, int sign) {
        this.userName = userName;
        this.userIllustration = userIllustration;
        this.isMan = isMan;
        this.icon = icon;
        this.sign = sign;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIllustration() {
        return userIllustration;
    }

    public void setUserIllustration(String userIllustration) {
        this.userIllustration = userIllustration;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        String str = userName + "/" + userIllustration + "/" + isMan + "/" + icon + "/" + sign + "/";
        return str;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIsMan() {
        return isMan;
    }

    public void setIsMan(String isMan) {
        this.isMan = isMan;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
