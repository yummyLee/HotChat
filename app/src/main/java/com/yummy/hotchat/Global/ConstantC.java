package com.yummy.hotchat.Global;

/**
 * 项目名称：HotChat2
 * 类描述：
 * 创建人：qq985
 * 创建时间：2016/11/27 22:10
 * 修改人：qq985
 * 修改时间：2016/11/27 22:10
 * 修改备注：
 */
public class ConstantC {

    public final static int WIFI_LIST_REFRESHED = 12345;
    public final static int WIFI_LIST_INIT = 12346;

    public final static int CLIENT_PREPARE = 12347;
    public final static int CLIENT_CONNECT_SERVER = 12348;

    public final static int SERVER_SIGN = 12349;
    public final static int CLIENT_SIGN = 12350;

    public final static int WIFI_AP_ENABLED = 12351;

    public final static int CHAT_SERVER_FILE_PICKED = 12352;
    public final static int CHAT_CLIENT_FILE_PICKED = 12353;

    public final static int INTENT_TO_CHAT_REQUEST_CODE = 22349;

    public final static int FILE_SCAN_COMPLETED = 12354;

    public final static int WIFI_CONNECT_FAILED = 12356;


    public final static String WIFI_AP_PRE = "HOT-";
    public final static String NO_USERS_TIPS = "当前聊天室空无一人";
    public final static String NOT_ROOM = "这不是一个热点聊天室";

    //SharedPreferences
    //用户信息
    public final static String USER_INFO = "userInfo";
    public final static String USER_NAME = "userName";
    public final static String USER_IS_LOGIN = "userIsLogin";
    public final static String USER_ILLUSTRATION = "userIllustration";
    public final static String USER_DEFAULT_NAME = "未登录";
    public final static String USER_DEFAULT_ILLUSTRATION = "这个用户很懒，什么都没留下";

    //登录框
    public final static String USER_NAME_THAN_TWO = "昵称长度应大于2个字";
    public final static String USER_OFF_TIPS = "未登录";

    //聊天室
    public final static int CLIENT_CONNECTED = 12355;
    public final static String USER_CONNECTED = "有人进入了聊天室";
    public final static String CONNECT_ROOM_OK = "成功连上聊天室";

}
