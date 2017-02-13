package com.yummy.hotchat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.yummy.hotchat.Adapter.WifiListAdapter;
import com.yummy.hotchat.Fragment.HotFiles;
import com.yummy.hotchat.Fragment.MySettings;
import com.yummy.hotchat.Global.ConstantC;
import com.yummy.hotchat.Fragment.Chat;
import com.yummy.hotchat.Fragment.WifiList;
import com.yummy.hotchat.Global.GenerateRandom;
import com.yummy.hotchat.Global.ToastMaker;
import com.yummy.hotchat.Object.SocketUser;
import com.yummy.hotchat.Socket.SocketClient;
import com.yummy.hotchat.Socket.SocketServer;
import com.yummy.hotchat.Widget.CreatingDialog;
import com.yummy.hotchat.Widget.TipsDialog;
import com.yummy.hotchat.WifiTool.WifiReceiver;
import com.yummy.hotchat.WifiTool.WifiTools;
import com.yummy.hotchat.Global.ConstantC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    final static String TAG = "MainActivity";
    private static final int REQUEST_SETTINGS = 2;
    private static final int REQUEST_LOCATION = 3;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_drawer_layout)
    DrawerLayout mainDrawerLayout;
    @Bind(R.id.fab_scan)
    com.getbase.floatingactionbutton.FloatingActionButton fabScan;
    @Bind(R.id.fab_new)
    com.getbase.floatingactionbutton.FloatingActionButton fabNew;
    @Bind(R.id.fam_main)
    FloatingActionsMenu famMain;
    @Bind(R.id.navigation_view)
    NavigationView navigationView;
    @Bind(R.id.frame_main)
    FrameLayout frameMain;
    @Bind(R.id.activity_main)
    LinearLayout activityMain;

    private RadioButton[] rbHeaders = new RadioButton[4];
    private CreatingDialog creatingDialog;


    private FragmentManager fragmentManager;

    private WifiList wifiList;
    private Chat chat;
    private HotFiles hotFiles;
    private MySettings mySettings;

    private Context context;

    private WifiTools wifiTools;
    public int sign = 0;

    private boolean isToServerConnected = false;
    private boolean isToClientConnected = false;

    private SocketClient socketClient;
    private SocketServer socketServer;

    private WifiReceiver wifiReceiver;

    //侧边栏
    private ImageView ivNavHeader;
    private TextView tvNavUserName;
    private TextView tvNavUserIllustration;
    private String userName;
    private String userIllustration;
    private String userIcon;
    private String sex;

    private SharedPreferences userSP;
    private SharedPreferences.Editor userEditor;

    private SocketUser socketUser;
    private boolean isToSorCConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = this;

        initParameter();
        initFragment();


        initToolBar();
        initDrawer();
        initLoginDialog();

        initNav();
        setOnClick();


    }

    /**
     * 监听来自子线程的消息
     *
     * @return
     */
    public Handler getMainHandler() {
        return mainHandler;
    }

    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantC.CLIENT_PREPARE:
                    new ConnectServer().start();
                    break;
                case ConstantC.CLIENT_CONNECT_SERVER:
                    wifiList.getConnectingDialog().dismiss();
                    ToastMaker.toastSomething(getApplicationContext(), ConstantC.CONNECT_ROOM_OK);
                    chat = new Chat();
                    fragmentManager.beginTransaction().add(R.id.frame_main, chat).commit();
                    chat.setSign(ConstantC.CLIENT_SIGN);
                    chat.setSocketUser(socketUser);
                    socketClient.setSocketUser(socketUser);
                    navigationView.getMenu().getItem(1).setChecked(true);
                    famMain.setVisibility(View.GONE);
                    break;
                case ConstantC.WIFI_AP_ENABLED:

                    socketServer.setSocketUser(socketUser);
                    chat = new Chat();
                    fragmentManager.beginTransaction().add(R.id.frame_main, chat).commit();
                    Log.i(TAG,"已经切换");
                    chat.setSign(ConstantC.SERVER_SIGN);
                    chat.setSocketUser(socketUser);
                    navigationView.getMenu().getItem(1).setChecked(true);
                    famMain.setVisibility(View.GONE);
                    creatingDialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化侧边栏
     */
    void initNav() {

        socketUser = new SocketUser(ConstantC.USER_DEFAULT_NAME, ConstantC.USER_DEFAULT_ILLUSTRATION, "man", "NOICON", GenerateRandom.randomW());

        userSP = getSharedPreferences(ConstantC.USER_INFO, Activity.MODE_PRIVATE);
        userEditor = userSP.edit();
        userEditor.putString(ConstantC.USER_IS_LOGIN, "no");
        userEditor.commit();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        tvNavUserName = (TextView) headerView.findViewById(R.id.tv_nv_user_name);
        ivNavHeader = (ImageView) headerView.findViewById(R.id.iv_nv_header);
        tvNavUserIllustration = (TextView) headerView.findViewById(R.id.tv_nv_user_illustration);
        ivNavHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainDrawerLayout.closeDrawers();
                initLoginDialog();
            }
        });

        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_wifi_list:
                        item.setChecked(true);
                        switchFragment(0);
                        famMain.setVisibility(View.VISIBLE);
                        mainDrawerLayout.closeDrawers();
                        return true;
                    case R.id.nav_chat_room:
                        if (userSP.getString(ConstantC.USER_IS_LOGIN, "no").equals("yes")) {
                            item.setChecked(true);
                            switchFragment(1);
                            famMain.setVisibility(View.GONE);
                            mainDrawerLayout.closeDrawers();
                            return true;
                        } else {
                            Log.i(TAG, "未登录");
                            ToastMaker.toastSomething(getApplication(), "未登录");
                        }
                        return false;
                    case R.id.nav_files:
                        item.setChecked(true);
                        switchFragment(2);
                        famMain.setVisibility(View.GONE);
                        mainDrawerLayout.closeDrawers();
                        return true;

                    case R.id.nav_settings:
                        item.setChecked(true);
                        switchFragment(3);
                        famMain.setVisibility(View.GONE);
                        mainDrawerLayout.closeDrawers();
                        return true;
                    default:
                        mainDrawerLayout.closeDrawers();
                        return false;
                }
            }
        });

        tvNavUserName.setText(ConstantC.USER_DEFAULT_NAME);
        tvNavUserIllustration.setText(ConstantC.USER_DEFAULT_ILLUSTRATION);

    }

    void switchFragment(int index) {

        switch (index) {
            case 0:
                if (chat != null) {
                    fragmentManager.beginTransaction().hide(chat).commit();
                }
                if (hotFiles != null) {
                    fragmentManager.beginTransaction().hide(hotFiles).commit();
                }
                if (mySettings != null) {
                    fragmentManager.beginTransaction().hide(mySettings).commit();
                }
                Log.i(TAG, "切换" + index);
                if (wifiList == null) {
                    wifiList = new WifiList();
                    fragmentManager.beginTransaction().add(R.id.frame_main, wifiList).commit();
                } else {
                    Log.i(TAG, "已初始化，切换" + index);
                    fragmentManager.beginTransaction().show(wifiList).commit();
                }
                break;
            case 1:
                if (chat != null) {
                    fragmentManager.beginTransaction().hide(hotFiles).commit();
                }
                if (hotFiles != null) {
                    fragmentManager.beginTransaction().hide(wifiList).commit();
                }
                if (mySettings != null) {
                    fragmentManager.beginTransaction().hide(mySettings).commit();
                }
                Log.i(TAG, "切换" + index);
                if (chat == null) {
                    chat = new Chat();
                    fragmentManager.beginTransaction().add(R.id.frame_main, chat).commit();
                } else {
                    Log.i(TAG, "已初始化，切换" + index);
                    fragmentManager.beginTransaction().show(chat).commit();
                }
                break;
            case 2:
                if (chat != null) {
                    fragmentManager.beginTransaction().hide(chat).commit();
                }
                if (hotFiles != null) {
                    fragmentManager.beginTransaction().hide(wifiList).commit();
                }
                if (mySettings != null) {
                    fragmentManager.beginTransaction().hide(mySettings).commit();
                }
                Log.i(TAG, "切换" + index);
                if (hotFiles == null) {
                    hotFiles = new HotFiles();
                    fragmentManager.beginTransaction().add(R.id.frame_main, hotFiles).commit();
                } else {
                    Log.i(TAG, "已初始化，切换" + index);
                    fragmentManager.beginTransaction().show(hotFiles).commit();
                }
                break;
            case 3:
                if (chat != null) {
                    fragmentManager.beginTransaction().hide(chat).commit();
                }
                if (hotFiles != null) {
                    fragmentManager.beginTransaction().hide(hotFiles).commit();
                }
                if (mySettings != null) {
                    fragmentManager.beginTransaction().hide(wifiList).commit();
                }
                Log.i(TAG, "切换" + index);
                if (mySettings == null) {
                    mySettings = new MySettings();
                    fragmentManager.beginTransaction().add(R.id.frame_main, mySettings).commit();
                } else {
                    Log.i(TAG, "已初始化，切换" + index);
                    fragmentManager.beginTransaction().show(mySettings).commit();
                }
                break;
            default:
                break;
        }

    }

    /**
     * 初始化相关的参数
     */
    void initParameter() {
        wifiTools = WifiTools.getInstance(this);
        socketClient = new SocketClient();
        socketServer = new SocketServer();
        wifiTools.closeHotSpot();
        wifiTools.openWifi();
        userIcon = "1";
        creatingDialog = new CreatingDialog(this, getLayoutInflater(), R.layout.dialog_creating, getResources().getString(R.string.dialog_creating_title));
    }

    /**
     * 为FloatingActionButton注册监听器
     */
    void setOnClick() {
        fabNew.setOnClickListener(this);
        fabScan.setOnClickListener(this);
    }

    /**
     * 初始化FragmentManager
     */
    void initFragment() {

        fragmentManager = getSupportFragmentManager();
        switchFragment(0);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    /**
     * 初始化登录的Dialog
     */
    void initLoginDialog() {

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_login_info, (ViewGroup) findViewById(R.id.dialog_login_info));

        rbHeaders[0] = (RadioButton) view.findViewById(R.id.rb_login_header1);
        rbHeaders[1] = (RadioButton) view.findViewById(R.id.rb_login_header2);
        rbHeaders[2] = (RadioButton) view.findViewById(R.id.rb_login_header3);
        rbHeaders[3] = (RadioButton) view.findViewById(R.id.rb_login_header4);

        rbHeaders[0].setChecked(true);

        rbHeaders[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < 4; i++) {
                        if (i != 0) {
                            rbHeaders[i].setChecked(false);
                            userIcon = "1";
                        }
                    }
                }
            }
        });

        rbHeaders[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < 4; i++) {
                        if (i != 1) {
                            rbHeaders[i].setChecked(false);
                            userIcon = "2";
                        }
                    }
                }
            }
        });

        rbHeaders[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < 4; i++) {
                        if (i != 2) {
                            rbHeaders[i].setChecked(false);
                            userIcon = "3";
                        }
                    }
                }
            }
        });

        rbHeaders[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < 4; i++) {
                        if (i != 3) {
                            rbHeaders[i].setChecked(false);
                            userIcon = "4";
                        }
                    }
                }
            }
        });

//        rbHeader1.setChecked(true);

        final RadioButton radioButtonMale = (RadioButton) view.findViewById(R.id.rb_male);
        radioButtonMale.setChecked(true);

        final TextInputLayout tilName = (TextInputLayout) view.findViewById(R.id.til_login_input_nickname);
        final TextInputLayout tilIllustration = (TextInputLayout) view.findViewById(R.id.til_login_input_illustration);
        final TextInputEditText tieName = (TextInputEditText) view.findViewById(R.id.tie_input_nickname);
        TextInputEditText tieIllustration = (TextInputEditText) view.findViewById(R.id.tie_input_illustration);

        tilName.setError(ConstantC.USER_NAME_THAN_TWO);
        tilName.setErrorEnabled(true);
        tieName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                tilName.setErrorEnabled(true);
//                tilName.setError(ConstantC.USER_NAME_THAN_TWO);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 2) {

                    tilName.setErrorEnabled(true);
                    tilName.setError(ConstantC.USER_NAME_THAN_TWO);

                } else {
                    tilName.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 2) {
                    tilName.setErrorEnabled(true);
                    tilName.setError(ConstantC.USER_NAME_THAN_TWO);
                } else {
                    tilName.setErrorEnabled(false);
                }
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("填写个人信息");
        builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!tilName.isErrorEnabled()) {
                    userName = tilName.getEditText().getText().toString();
                    userIllustration = tilIllustration.getEditText().getText().toString();
                    if (userIllustration.equals("")) {
                        userIllustration = ConstantC.USER_DEFAULT_ILLUSTRATION;
                    }
                    Log.i(TAG, "用户名为：" + userName + ",个人说明为：" + userIllustration + "sign=" + socketUser.getSign());
                    //存储SP
                    userEditor.putString(ConstantC.USER_NAME, userName);
                    userEditor.putString(ConstantC.USER_IS_LOGIN, "yes");
                    userEditor.putString(ConstantC.USER_ILLUSTRATION, userIllustration);
                    userEditor.commit();
                    tvNavUserName.setText(userName);
                    tvNavUserIllustration.setText(userIllustration);
                    switch (userIcon) {
                        case "1":
                            ivNavHeader.setImageResource(R.drawable.header);
                            break;
                        case "2":
                            ivNavHeader.setImageResource(R.drawable.header2);
                            break;
                        case "3":
                            ivNavHeader.setImageResource(R.drawable.header3);
                            break;
                        case "4":
                            ivNavHeader.setImageResource(R.drawable.header4);
                            break;
                        default:
                            break;
                    }

                    if (radioButtonMale.isChecked()) {
                        sex = "man";
                        socketUser.setIsMan("man");
                    } else {
                        sex = "woman";
                        socketUser.setIsMan("woman");
                    }
                    socketUser.setUserName(userName);
                    socketUser.setUserIllustration(userIllustration);
                    socketUser.setIcon(userIcon);
                    //传给SocketClient和SocketServer
                    socketClient.setSocketUser(socketUser);
                    socketServer.setSocketUser(socketUser);
                } else {
                    ToastMaker.toastSomething(getApplication(), ConstantC.USER_NAME_THAN_TWO);
                }
            }
        });
        builder.setView(view);
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 初始化Drawer 设置右上角的按钮
     */
    void initDrawer() {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mainDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    /**
     * 初始化toolbar
     */
    void initToolBar() {

        setSupportActionBar(toolbar);

    }


    /**
     * 设置显示右上角的菜单，并设置点击效果
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.tm_test01:
                wifiTools.openWifi();
                Log.i(TAG, getString(R.string.tm_item1_clicked));
                break;
            case R.id.tm_test02:
                wifiTools.closeWifi();
                Log.i(TAG, getString(R.string.tm_item2_clicked));
                break;
            case R.id.tm_test03:
                wifiTools.closeHotSpot();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_scan:
                Log.i(TAG, "fab_scan_clicked");
                break;
            case R.id.fab_new:
                Log.i(TAG, "fab_new_clicked");
                if (checkWriteSettingsPermission()) {
                    createChatRoom();
                } else {
                    Log.i(TAG, "没有WRITE_SETTINGS权限");
                }
                break;
            default:
                Log.i(TAG, "fab_scan_error");
                break;
        }
    }

    /**
     * 创建一个聊天室
     */
    void createChatRoom() {

        if (userSP.getString(ConstantC.USER_IS_LOGIN, "no").equals("yes")) {
            LayoutInflater inflater = getLayoutInflater();
            View view1 = inflater.inflate(R.layout.dialog_create_ap, (ViewGroup) findViewById(R.id.dialog_create_room_info));

            final TextInputLayout tilRoomName = (TextInputLayout) view1.findViewById(R.id.til_create_input_room_name);
            final TextInputEditText tieRoomName = (TextInputEditText) view1.findViewById(R.id.tie_create_input_room_name);
            final TextInputLayout tilRoomPassword = (TextInputLayout) view1.findViewById(R.id.til_create_input_room_password);
            final TextInputEditText tieRoomPassword = (TextInputEditText) view1.findViewById(R.id.tie_create_input_room_password);

            final RadioButton rbWithPassword = (RadioButton) view1.findViewById(R.id.rb_create_with_password);
            final RadioButton rbNoPassword = (RadioButton) view1.findViewById(R.id.rb_create_no_password);

            rbNoPassword.setChecked(true);

            //如果选择了有密码，且当前密码小于8，提示错误
            rbWithPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.i(TAG, "选择了有密码");
                        if (tieRoomPassword.getText().length() < 8) {
                            tilRoomPassword.setErrorEnabled(true);
                            tilRoomPassword.setError(getString(R.string.dialog_create_less_than8));
                        } else {

                        }
                    } else {

                    }
                }
            });

            //如果选择无密码则取消错误提示
            rbNoPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.i(TAG, "选择了无密码");
                        tilRoomPassword.setErrorEnabled(false);
                    } else {

                    }
                }
            });

            tilRoomName.setErrorEnabled(true);
            tilRoomName.setError(getResources().getString(R.string.dialog_create_less_than2));
            tieRoomName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                tilRoomName.setError(getResources().getString(R.string.dialog_create_less_than2));
//                tilRoomName.setErrorEnabled(true);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() < 2) {
                        tilRoomName.setError(getResources().getString(R.string.dialog_create_less_than2));
                        tilRoomName.setErrorEnabled(true);
                    } else {
                        tilRoomName.setErrorEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() < 2) {
                        tilRoomName.setError(getResources().getString(R.string.dialog_create_less_than2));
                        tilRoomName.setErrorEnabled(true);
                        Log.i(TAG, getString(R.string.dialog_create_less_than2));
                    } else {
                        tilRoomName.setErrorEnabled(false);
                    }
                }
            });


            tieRoomPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (rbWithPassword.isChecked()) {
                        //若选择了有密码
                        if (s.length() < 8) {
                            //若密码长度小于8
                            tilRoomPassword.setError(getString(R.string.dialog_create_less_than8));
                            tilRoomPassword.setErrorEnabled(true);
                        } else {
                            tilRoomPassword.setErrorEnabled(false);
                        }
                    } else {
                        tilRoomPassword.setErrorEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("创建", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int which) {
                    if (tilRoomName.isErrorEnabled()) {
                        Log.i(TAG, getResources().getString(R.string.dialog_create_less_than2));
                        ToastMaker.toastSomething(getApplicationContext(), getResources().getString(R.string.dialog_create_less_than2));
                    } else {
                        wifiTools = WifiTools.getInstance(context);
                        Log.i(TAG, "创建聊天室" + tieRoomName.getText().toString());
                        WifiConfiguration wifiConfiguration;
                        if (rbWithPassword.isChecked()) {
                            wifiConfiguration = wifiTools.createWifiInfo(ConstantC.WIFI_AP_PRE + tieRoomName.getText().toString(), tieRoomPassword.getText().toString(), "WifiAP", wifiTools.WIFICIPHER_WPA2);
                        } else {
                            wifiConfiguration = wifiTools.createWifiInfo(ConstantC.WIFI_AP_PRE + tieRoomName.getText().toString(), "", "WifiAP", wifiTools.WIFICIPHER_NOPASS);
                        }
                        wifiTools.createHotSpot(wifiConfiguration);
                        creatingDialog.initParameterAndShow();

                        new Thread() {
                            @Override
                            public void run() {
                                System.out.println("createServer");
                                while (wifiTools.getWifiApState() != wifiTools.WIFI_AP_STATE_ENABLED) {

                                    try {
                                        Thread.sleep(500);
                                        Log.i(TAG, "wait_ap_enabled");
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

//                                chat.setSsid(tieRoomName.getText().toString());
                                Message msg = new Message();
                                msg.what = ConstantC.WIFI_AP_ENABLED;
                                mainHandler.sendMessage(msg);

                                socketServer.createServer();
//                                    chat.setSocketServer(socketServer);
                            }
                        }.start();


                    }
                }
            });
            builder.setNegativeButton("取消", null);
            builder.setTitle("聊天室信息");
            builder.setView(view1);
            builder.show();
        } else {
            ToastMaker.toastSomething(getApplicationContext(), "未登录");
            Log.e(TAG, "未登录，不能创建");
        }
    }

    class ConnectServer extends Thread {

        @Override
        public void run() {
            super.run();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            while (wifiTools.getConnectedHotIP().size() < 2) {
                try {
                    Thread.sleep(500);
                    System.out.println("wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            socketClient = new SocketClient();
            socketClient.createClient(wifiTools.getConnectedHotIP().get(1));
            Message msg = new Message();
            msg.what = ConstantC.CLIENT_CONNECT_SERVER;
            mainHandler.sendMessage(msg);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isToClientConnected) {
//            socketServer.createServer();
        }
        if (isToServerConnected) {
            Log.i(TAG, "返回重连socketServer");
            socketClient.createClient(wifiTools.getConnectedHotIP().get(1));
        } else {
            Log.i(TAG, "返回前未连接socketServer");
        }
    }

    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    private long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - firstTime > 2000) {
                ToastMaker.toastSomething(MainActivity.this, "再按一次返回键退出程序");
                firstTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    public boolean checkWriteSettingsPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "该设备是安卓6.0及以上");
            if (!Settings.System.canWrite(this)) {

                TipsDialog tipsDialog = new TipsDialog(this, getLayoutInflater(), R.layout.dialog_tips, "请授予获取位置信息权限", "安卓6.0以上系统扫描Wifi需要开启位置权限，请手动打开~", "前去设置", "取消");
                tipsDialog.initParameterAndShow();
                tipsDialog.setOnTipsDialogListener(new TipsDialog.OnTipsDialogClickedListener() {
                    @Override
                    public void onPositiveClicked() {

                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, REQUEST_SETTINGS);
                    }

                    @Override
                    public void onNegativeClicked() {

                    }
                });

                return false;

            } else {
                Log.i(TAG, "热点开启");
                return true;
//                wifiTools.createHotSpot(wifiTools.createWifiInfo("Test111", "12345678", "WifiAP", wifiTools.WIFICIPHER_WPA2));
            }
        } else {

            Log.i(TAG, "设备版本小于安卓6.0");
            Log.i(TAG, "热点开启");
            return true;
        }
    }


    public SocketServer getSocketServer() {
        return socketServer;
    }

    public SocketClient getSocketClient() {
        return socketClient;
    }

    public SocketUser getSocketUser() {
        return socketUser;
    }

    /**
     * 初始化CircularFloatingButton
     * 直接调用即可
     */
    private void initFloatingButton() {

        ImageView icon = new ImageView(this); // Create an icon
        icon.setImageResource(R.drawable.plus);

        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        // repeat many times:
        ImageView itemIcon1 = new ImageView(this);
        itemIcon1.setImageResource(R.drawable.wifi_green1);
        SubActionButton button1 = itemBuilder.setContentView(itemIcon1).build();

        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.wifi_green1);
        SubActionButton button2 = itemBuilder.setContentView(itemIcon2).build();
        ImageView itemIcon3 = new ImageView(this);
        itemIcon3.setImageResource(R.drawable.wifi_green1);
        SubActionButton button3 = itemBuilder.setContentView(itemIcon3).build();
        ImageView itemIcon4 = new ImageView(this);
        itemIcon4.setImageResource(R.drawable.wifi_green1);
        SubActionButton button4 = itemBuilder.setContentView(itemIcon4).build();


        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .addSubActionView(button4)
                .attachTo(actionButton)
                .build();
    }

}
