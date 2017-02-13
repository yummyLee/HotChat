package com.yummy.hotchat.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yummy.hotchat.Adapter.WifiListAdapter;
import com.yummy.hotchat.Global.ConstantC;
import com.yummy.hotchat.Global.ToastMaker;
import com.yummy.hotchat.MainActivity;
import com.yummy.hotchat.Object.ScanResultWithLock;
import com.yummy.hotchat.R;
import com.yummy.hotchat.Socket.SocketClient;
import com.yummy.hotchat.Socket.SocketServer;
import com.yummy.hotchat.Widget.CreatingDialog;
import com.yummy.hotchat.Widget.TipsDialog;
import com.yummy.hotchat.WifiTool.SortResultsByLevel;
import com.yummy.hotchat.WifiTool.WifiReceiver;
import com.yummy.hotchat.WifiTool.WifiTools;

import java.io.Serializable;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import butterknife.ButterKnife;


public class WifiList extends Fragment implements View.OnClickListener {


    private RecyclerView rvWifiList;
    private SwipeRefreshLayout srlWifiList;
    private CreatingDialog connectingDialog;
    private TextView tvTips;

    private View view;
    private static final int REQUEST_SETTINGS = 2;
    private static final int REQUEST_LOCATION = 3;
    private static final int REQUEST_WIFI = 4;
    static final String TAG = "WifiList";

    private Context context;

    private List<ScanResultWithLock> scanResultWithLocks;
    private WifiListAdapter wifiListAdapter;

    private WifiReceiver wifiReceiver;

    private WifiTools wifiTools;

    private SharedPreferences userSP;
    private SharedPreferences.Editor userEditor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wifi_list, container, false);

        rvWifiList = (RecyclerView) view.findViewById(R.id.rv_wifi_list);
        srlWifiList = (SwipeRefreshLayout) view.findViewById(R.id.srl_wifi_list);
        tvTips = (TextView) view.findViewById(R.id.tv_wifi_list_tips);

        ButterKnife.bind(getActivity());
        initParameter();
        initWifiListAdapter();

        return view;
    }

    private Handler listHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                //wifi列表刷新处理

                case ConstantC.WIFI_LIST_REFRESHED:

                    int size = scanResultWithLocks.size();
                    Log.i(TAG, "主线程中获取到扫描结果大小：" + size);
                    if (size == 0) {
                        ToastMaker.toastSomething(getActivity(), "请确定wifi是否打开!");
                        tvTips.setVisibility(View.VISIBLE);
                    } else {
                        tvTips.setVisibility(View.VISIBLE);
                    }
                    wifiListAdapter.setData(scanResultWithLocks);
                    wifiTools.getConfiguration();
                    wifiListAdapter.notifyDataSetChanged();
                    srlWifiList.setRefreshing(false);

                    break;
                //wifi连接失败
                case ConstantC.WIFI_CONNECT_FAILED:
                    connectingDialog.dismiss();
                    ToastMaker.toastSomething(getActivity(), "进入聊天室失败，请确认密码是否正确！");

                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化WifiTool参数
     */
    private void initParameter() {

        userSP = getActivity().getSharedPreferences(ConstantC.USER_INFO, Activity.MODE_PRIVATE);
        userEditor = userSP.edit();
        userEditor.commit();

        wifiTools = WifiTools.getInstance(getActivity());
        context = getActivity();
        wifiReceiver = new WifiReceiver();
        wifiReceiver.setOnWifiChangedListener(new WifiReceiver.OnWifiChangedListener() {
            @Override
            public void onWifiOpened() {
                new RefreshWifiList().start();
            }

            @Override
            public void onWifiClosed() {

            }
        });
    }


    /**
     * 初始化wifi列表的
     */
    void initWifiListAdapter() {

        scanResultWithLocks = new ArrayList<>();
        wifiListAdapter = new WifiListAdapter(getActivity(), scanResultWithLocks);

        wifiListAdapter.setOnRItemClickListener(new WifiListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onNameClick(int position) {
                Log.i(TAG, position + ":tvName Clicked");
            }

            @Override
            public void onImageClick(int position) {
                Log.i(TAG, position + ":ivIcon Clicked");
            }

            @Override
            public void onConnectClick(final int position) {
                Log.i(TAG, position + ":tvConnect Clicked");

                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.dialog_connect_wifi, (ViewGroup) getActivity().findViewById(R.id.dialog_connect));
                final TextInputEditText textInputEditText = (TextInputEditText) view.findViewById(R.id.tie_connect_input_password);
                final TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.til_connect_input_password);
                TextView textView = (TextView) view.findViewById(R.id.tv_connect_ssid);
                textView.setText("SSID: " + scanResultWithLocks.get(position).getScanResult().SSID);
                final boolean isLocked = scanResultWithLocks.get(position).isLocked();
                if (!isLocked) {
                    textInputLayout.setVisibility(View.GONE);
                }

                textInputEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                        textInputLayout.setError(getResources().getString(R.string.dialog_connect_less_than8));
//                        textInputLayout.setErrorEnabled(true);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() < 8) {
                            textInputLayout.setError(getResources().getString(R.string.dialog_connect_less_than8));
                            textInputLayout.setErrorEnabled(true);
                        } else {
                            textInputLayout.setErrorEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (scanResultWithLocks.get(position).getScanResult().SSID.startsWith("HOT-")) {
                            if (userSP.getString(ConstantC.USER_IS_LOGIN, "no").equals("yes")) {
                                System.out.println(scanResultWithLocks.get(position).getScanResult().toString());
                                final String SSID = scanResultWithLocks.get(position).getScanResult().SSID;
                                final String passWord = textInputEditText.getText().toString();
                                connectingDialog = new CreatingDialog(getActivity(), getActivity().getLayoutInflater(), R.layout.dialog_creating, getResources().getString(R.string.dialog_connecting_title));
                                connectingDialog.initParameterAndShow();
                                final int wifiItemId = wifiTools.IsConfiguration("\"" + SSID + "\"");

                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        if (!isLocked) {
                                            Log.i(TAG, "该聊天室没有密码");
                                            wifiTools.connectWifi(SSID, "", WifiTools.WIFICIPHER_NOPASS);
                                            System.out.println(wifiTools.getConnectedHotIP());
                                            MainActivity mainActivity = (MainActivity) getActivity();

                                            Message message = new Message();
                                            message.what = ConstantC.CLIENT_PREPARE;
                                            mainActivity.getMainHandler().sendMessage(message);
                                        } else {
                                            Log.i(TAG, "从来没有连过这个聊天室");
                                            int netId = wifiTools.AddWifiConfig(scanResultWithLocks, SSID, passWord);
                                            if (netId != -1) {
                                                Log.i(TAG, "创建配置信息成功");
                                                wifiTools.getConfiguration();//添加了配置信息，要重新得到配置信息
                                                wifiTools.connectWifi(netId);
                                                System.out.println(wifiTools.getConnectedHotIP());
                                                MainActivity mainActivity = (MainActivity) getActivity();

                                                Message message = new Message();
                                                message.what = ConstantC.CLIENT_PREPARE;
                                                mainActivity.getMainHandler().sendMessage(message);

                                            } else {
                                                Message msg = new Message();
                                                msg.what = ConstantC.WIFI_CONNECT_FAILED;
                                                listHandler.sendMessage(msg);
                                            }
                                        }
                                    }
                                }.start();
                            } else {
                                ToastMaker.toastSomething(getActivity(), ConstantC.USER_OFF_TIPS);
                                Log.e(TAG, "没有登录不能连接");
                            }
                        } else {
                            ToastMaker.toastSomething(getActivity(), ConstantC.NOT_ROOM);
                            Log.e(TAG, ConstantC.NOT_ROOM);
                        }

                    }
                });
                builder.setNegativeButton("取消", null);
                if (isLocked) {
                    builder.setTitle("私密聊天室");
                } else {
                    builder.setTitle("公开聊天室");
                }
                builder.setCancelable(false);


                builder.setView(view);
                builder.show();


            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //设置布局管理器
        rvWifiList.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        rvWifiList.setAdapter(wifiListAdapter);
        //设置增加或删除条目的动画
        rvWifiList.setItemAnimator(new DefaultItemAnimator());


//        srlWifiList.setColorSchemeColors();  //开始用这个方法，没有用
        srlWifiList.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);

        srlWifiList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Log.i(TAG, "WifiList 刷新");
                if (checkLocationPermission()) {

                    new RefreshWifiList().start();

                } else {
                    srlWifiList.setRefreshing(false);
                    Log.i(TAG, "没有位置权限");
                }

            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 刷新wifi列表的线程
     */
    class RefreshWifiList extends Thread {
        @Override
        public void run() {
            try {
                requestWifiPermission();
                Log.i(TAG, "RecycleView 刷新");

                wifiTools.startScanWifi();
                Thread.sleep(1500);
                List<ScanResult> scanResults = wifiTools.scanWifi();
                Log.i(TAG, "扫描结果大小：" + scanResults.size());
                Collections.sort(scanResults, new SortResultsByLevel());
                scanResultWithLocks = new ArrayList<>();
                for (ScanResult scanResult : scanResults) {
                    boolean isLocked;
                    if (scanResult.capabilities.length() <= 5) {
                        isLocked = false;
                    } else {
                        isLocked = true;
                    }
                    ScanResultWithLock scanResultWithLock = new ScanResultWithLock(scanResult, isLocked);
                    scanResultWithLocks.add(scanResultWithLock);
                }

                Log.i(TAG, "扫描结果大小：" + scanResultWithLocks.size());

                Message msg = new Message();
                msg.what = ConstantC.WIFI_LIST_REFRESHED;
                listHandler.sendMessage(msg);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用于检测是否有获取位置信息的权限（6.0以上）
     *
     * @return
     */
    public boolean checkLocationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "版本大于6.0");
            Log.i(TAG, "获取wifi扫描结果,判断是否有权限");
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "无权限，请打开权限");

                TipsDialog tipsDialog = new TipsDialog(getActivity(), getActivity().getLayoutInflater(), R.layout.dialog_tips, "请授予获取位置信息权限", "安卓6.0以上系统扫描Wifi需要开启位置权限，请手动打开~", "前去设置", "取消");
                tipsDialog.initParameterAndShow();
                tipsDialog.setOnTipsDialogListener(new TipsDialog.OnTipsDialogClickedListener() {
                    @Override
                    public void onPositiveClicked() {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getActivity().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, REQUEST_LOCATION);
                    }

                    @Override
                    public void onNegativeClicked() {

                    }
                });

                return false;

            } else {

                Log.i(TAG, "有权限，判断是否打开定位");
                if (wifiTools.getGpsState(getActivity())) {
                    Log.i(TAG, "定位已打开");
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                    ToastMaker.toastSomething(getActivity(), "定位未打开，请打开定位");
                    Log.i(TAG, "定位未打开");
                }
                return true;
            }
        } else {
            Log.i(TAG, "版本小于6.0");
            return true;
        }
    }

    /**
     * 获取wifi权限
     */
    private void requestWifiPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG, "无Wifi权限");
            requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_WIFI);
        } else {
            Log.i(TAG, "有Wifi权限");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_WIFI:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new RefreshWifiList().start();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 返回正在连接聊天室的窗口以在主activity关闭
     *
     * @return
     */
    public CreatingDialog getConnectingDialog() {
        return connectingDialog;
    }


}
