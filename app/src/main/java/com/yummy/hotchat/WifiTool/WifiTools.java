package com.yummy.hotchat.WifiTool;

import android.content.Context;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.yummy.hotchat.Object.ScanResultWithLock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yummy on 16-11-5.
 */

public class WifiTools {

    private static WifiTools wifiTools = null;
    private static final String SETUP_WIFIAP_METHOD = "setWifiApEnabled";
    private static final String ACTIVITY_TAG = "WifiTools";
    private static final String DEFAULT_AP_PASSWORD = "12345678";


    //监听wifi热点的状态变化
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WPA = 2;
    public static final int WIFICIPHER_WEP = 3;
    public static final int WIFICIPHER_INVALID = 4;
    public static final int WIFICIPHER_WPA2 = 5;


    public WifiManager wifiManager;
    //describe the state of wifi connection
    private WifiInfo wifiInfo;
    private TelephonyManager telephonyManager;
    private List<ScanResult> scanResults;
    private List<WifiConfiguration> wifiConfigList;//WIFIConfiguration描述WIFI的链接信息，包括SSID、SSID隐藏、password等的设置

    private ConnectivityManager connectivityManager;

    private boolean connectedSign;


    public static WifiTools getInstance(Context context) {

        if (wifiTools == null) {
            wifiTools = new WifiTools(context);
            return wifiTools;
        } else {
            return wifiTools;
        }
    }

    public WifiTools(Context context) {

        connectedSign = false;

        //get system Wifi service WIFI_SERVICE
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //get connection info
        this.wifiInfo = this.wifiManager.getConnectionInfo();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    /**
     * 创建热点
     * @param wifiConfiguration wifi配置信息
     * @return
     */
    public boolean createHotSpot(WifiConfiguration wifiConfiguration) {
        try {
            closeWifi();
            closeHotSpot();
            Method setupMethod = wifiManager.getClass().getMethod(SETUP_WIFIAP_METHOD, WifiConfiguration.class, boolean.class);
            setupMethod.invoke(wifiManager, wifiConfiguration, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭热点
     *
     * @return
     */
    public boolean closeHotSpot() {
        Log.i(ACTIVITY_TAG, "closeHotSpot---" + getWifiApState());
        while (getWifiApState() == WIFI_AP_STATE_ENABLED) {
            Log.i(ACTIVITY_TAG, "AP state---" + getWifiApState() + "");
            try {
                Method method1 = wifiManager.getClass().getMethod("setWifiApEnabled",
                        WifiConfiguration.class, boolean.class);
                method1.invoke(wifiManager, null, false);

            } catch (Exception e) {
                e.printStackTrace();
//                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * 构造wifi信息
     *
     * @param ssid
     * @param password
     * @param type
     * @param passType
     * @return
     */
    public WifiConfiguration createWifiInfo(String ssid, String password, String type, int passType) {
        //配置网络信息类
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        //设置网络属性
        wifiConfiguration.allowedAuthAlgorithms.clear();
        wifiConfiguration.allowedGroupCiphers.clear();
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedPairwiseCiphers.clear();
        wifiConfiguration.allowedProtocols.clear();

        if (type.equals("WifiAP")) {
            Log.i(ACTIVITY_TAG, "创建热点配置信息");
            if (password.length() >= 8) {
                wifiConfiguration.preSharedKey = password;
            } else {
                wifiConfiguration.preSharedKey = DEFAULT_AP_PASSWORD;
            }
            wifiConfiguration.SSID = ssid;
//            wifiConfiguration.SSID = "\"" + ssid + "\"";
            switch (passType) {
                case WIFICIPHER_NOPASS:
                    Log.i(ACTIVITY_TAG, "创建无密码热点配置信息");
//            wifiConfiguration.hiddenSSID = true;
                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                    break;
                case WIFICIPHER_WPA2:
                    Log.i(ACTIVITY_TAG, "创建WPA2-PSK热点配置信息");

                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                    // 设置wifi热点密码
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wifiConfiguration.preSharedKey = password;

                    break;
                default:
                    Log.e(ACTIVITY_TAG, "创建热点配置信息出错");
                    break;

            }

        } else if (type.equals("Wifi")) {
            Log.i(ACTIVITY_TAG, "创建wifi连接配置信息");
            wifiConfiguration.SSID = ssid;
            wifiConfiguration.SSID = "\"" + ssid + "\"";
            switch (passType) {
                case WIFICIPHER_NOPASS:
                    Log.i(ACTIVITY_TAG, "创建 无密码 wifi配置信息");
//                    wifiConfiguration.wepKeys[0] = "";
                    wifiConfiguration.wepKeys[0] = "\"" + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.wepTxKeyIndex = 0;
                    break;
                case WIFICIPHER_WPA2:
                    Log.i(ACTIVITY_TAG, "创建 WPA-PSK wifi配置信息");
                    wifiConfiguration.preSharedKey = password;
//                    wifiConfiguration.preSharedKey = "\"" + password + "\"";
//                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//                    wifiConfiguration.allowedKeyManagement.set(4);
////                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
////                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

                    wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                    wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);

                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    // 必须添加，否则无线路由无法连接
                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

                    break;
                default:
                    Log.e(ACTIVITY_TAG, "创建wifi连接配置信息出错");
                    break;
            }

        } else {
            Log.i(ACTIVITY_TAG, "wifi配置信息 type 异常");
        }
        return wifiConfiguration;
    }

    //判断曾经连接过得WiFi中是否存在指定SSID的WifiConfiguration
    public WifiConfiguration isExists(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(SSID)) {
                return existingConfig;
            }
//            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
//                return existingConfig;
//            }
        }
        return null;
    }


    /**
     * 扫描wifi
     *
     * @return
     */
    public List<ScanResult> scanWifi() {
        Log.i(ACTIVITY_TAG, "scanWifi");
        scanResults = wifiManager.getScanResults();
        Log.i(ACTIVITY_TAG, "ScanResults Size:" + scanResults.size());
//        for (ScanResult scanResult : scanResults) {
////            System.out.println(calculateSignalLevel(scanResult.level, 4));
//            System.out.println(scanResult.toString());
//            System.out.println(scanResult.capabilities.length());
//        }
        return scanResults;
    }

    public void startScanWifi(){
        wifiManager.startScan();
    }

    /**
     * 连接wifi
     *
     * @param ssid
     * @param password
     * @param type     安全类型
     * @return
     */
    public boolean connectWifi(String ssid, String password, int type) {
        Log.i(ACTIVITY_TAG, "连接到指定wifi");
        openWifi();
        WifiConfiguration tempConfig = isExists(ssid);
        if (tempConfig == null) {
            WifiConfiguration wifiConfiguration = createWifiInfo(ssid, password, "Wifi", type);
            int WCID = wifiManager.addNetwork(wifiConfiguration);
//        wifiManager.enableNetwork(wifiConfiguration.networkId,true);
            System.out.println(WCID);
            new connectToWifi().execute(WCID);
        } else {
            //发现指定WiFi，并且这个WiFi以前连接成功过
            Log.i(ACTIVITY_TAG, "发现指定WiFi，并且这个WiFi以前连接成功过");
            WifiConfiguration wifiConfiguration = tempConfig;
//            new connectToWifi().execute(wifiConfiguration.networkId);
            return wifiManager.enableNetwork(wifiConfiguration.networkId, true);
        }

        return false;
    }

    class connectToWifi extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            Log.i(ACTIVITY_TAG, "异步连接Wifi");
            System.out.println(integers[0]);
            System.out.println(wifiManager.enableNetwork(integers[0], true));
            connectedSign = true;
            return null;
        }
    }


    //得到Wifi配置好的信息
    public void getConfiguration() {
        wifiConfigList = wifiManager.getConfiguredNetworks();//得到配置好的网络信息
//        for(int i =0;i<wifiConfigList.size();i++){
//            Log.i("getConfiguration",wifiConfigList.get(i).SSID);
//            Log.i("getConfiguration",String.valueOf(wifiConfigList.get(i).networkId));
//        }
    }

    //判定指定WIFI是否已经配置好,依据WIFI的地址BSSID,返回NetId
    public int IsConfiguration(String SSID) {
//        Log.i("IsConfiguration",String.valueOf(wifiConfigList.size()));
        for (int i = 0; i < wifiConfigList.size(); i++) {
//            Log.i(wifiConfigList.get(i).SSID, String.valueOf(wifiConfigList.get(i).networkId));
            if (wifiConfigList.get(i).SSID.equals(SSID)) {//地址相同
                return wifiConfigList.get(i).networkId;
            }
        }
        return -1;
    }

    //添加指定WIFI的配置信息,原列表不存在此SSID
    public int AddWifiConfig(List<ScanResultWithLock> wifiList, String ssid, String pwd) {
        int wifiId = -1;
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult wifi = wifiList.get(i).getScanResult();
            if (wifi.SSID.equals(ssid)) {
//                Log.i("AddWifiConfig","equals");
                WifiConfiguration wifiCong = new WifiConfiguration();
                wifiCong.SSID = "\"" + wifi.SSID + "\"";//\"转义字符，代表"
                wifiCong.preSharedKey = "\"" + pwd + "\"";//WPA-PSK密码
                wifiCong.hiddenSSID = false;
                wifiCong.status = WifiConfiguration.Status.ENABLED;
                wifiId = wifiManager.addNetwork(wifiCong);//将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
                if (wifiId != -1) {
                    return wifiId;
                }
            }
        }
        return wifiId;
    }

    //连接指定Id的WIFI
    public boolean connectWifi(int wifiId) {
        for (int i = 0; i < wifiConfigList.size(); i++) {
            WifiConfiguration wifi = wifiConfigList.get(i);
            if (wifi.networkId == wifiId) {
                return wifiManager.enableNetwork(wifiId, true);
            }
        }
        return false;
    }


    /**
     * 打开 wifi
     *
     * @return
     */
    public boolean openWifi() {
        if (!wifiManager.isWifiEnabled()) { //the current wifi is disabled
            wifiManager.setWifiEnabled(true);
            Log.i(ACTIVITY_TAG, "wifi opened!");
            return true;
        } else {
            wifiManager.setWifiEnabled(true);
            Log.i(ACTIVITY_TAG, "wifi had been opened!");
        }
        return false;
    }

    /**
     * 关闭 wifi
     *
     * @return
     */
    public boolean closeWifi() {
        if (wifiManager.isWifiEnabled()) { //the current wifi is enabled
            wifiManager.setWifiEnabled(false);
            Log.i(ACTIVITY_TAG, "wifi closed!");
            return true;
        } else {
            Log.i(ACTIVITY_TAG, "wifi had been closed!");
        }
        return false;
    }

    /**
     * 获取热点创建状态
     **/
    public int getWifiApState() {
        try {
            int i = ((Integer) this.wifiManager.getClass()
                    .getMethod("getWifiApState", new Class[0])
                    .invoke(this.wifiManager, new Object[0])).intValue();
            return i;
        } catch (Exception localException) {
        }
        return 4;   //未知wifi网卡状态
    }

    /**
     * 获取当前热点的密码
     *
     * @return
     */
    public String getValidPassword() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfigurationuration = (WifiConfiguration) method.invoke(wifiManager);
            return wifiConfigurationuration.preSharedKey;
        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(ACTIVITY_TAG, e.getMessage());
            return null;
        }

    }

    /**
     * 获取当前移动数据开启状态
     *
     * @param context
     * @return
     */
    public boolean getMobileDataEnabled(Context context) {
        try {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyManager.getClass().getMethod("getDataEnabled");

            return (Boolean) getMobileDataEnabledMethod.invoke(telephonyManager);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(ACTIVITY_TAG, "Error setting mobile data state", e);
        }
        return true;
    }

    /**
     * 获取ip地址
     **/
    public int getIPAddress() {
        return (wifiInfo == null) ? 0 : wifiInfo.getIpAddress();
    }

    /**
     * 获取物理地址(Mac)
     **/
    public String getMacAddress() {
        return (wifiInfo == null) ? "NULL" : wifiInfo.getMacAddress();
    }

    /**
     * 获取网络id
     **/
    public int getNetworkId() {
        return (wifiInfo == null) ? 0 : wifiInfo.getNetworkId();
    }

//    public void TestConnectionInfo(){
//        wifiInfo.
//    }

    public ArrayList<String> getConnectedHotIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }

    //输出链接到当前设备的IP地址
    public int printHotIp() {

        Log.i(ACTIVITY_TAG, "打印IP");

        ArrayList<String> connectedIP = getConnectedHotIP();
        StringBuilder resultList = new StringBuilder();
        for (String ip : connectedIP) {
            resultList.append(ip);
            resultList.append("\n");
        }
        System.out.println(resultList);

        return resultList.length();
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean getGpsState(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是平板
     *
     * @param context
     * @return
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    /**
     * 计算信号强度
     *
     * @param rssi
     * @param numLevels
     * @return
     */
    public static int calculateSignalLevel(int rssi, int numLevels) {
        return WifiManager.calculateSignalLevel(rssi, numLevels) + 1;
    }

    public boolean isConnectedSign() {
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    System.out.println("wifi已连接");
                    return true;
                }
                return false;
            }
        }

        return false;
    }


}