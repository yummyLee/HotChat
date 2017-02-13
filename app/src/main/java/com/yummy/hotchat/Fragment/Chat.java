package com.yummy.hotchat.Fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yummy.hotchat.Adapter.ChatAdapter;
import com.yummy.hotchat.Global.ConstantC;
import com.yummy.hotchat.Global.GetCurrentTime;
import com.yummy.hotchat.Global.MessageType;
import com.yummy.hotchat.Global.ToastMaker;
import com.yummy.hotchat.MainActivity;
import com.yummy.hotchat.Model.ChatModel;
import com.yummy.hotchat.Model.ItemModel;
import com.yummy.hotchat.Object.SocketUser;
import com.yummy.hotchat.R;
import com.yummy.hotchat.Socket.SocketClient;
import com.yummy.hotchat.Socket.SocketServer;
import com.yummy.hotchat.Global.TestData;
import com.yummy.hotchat.Widget.UserInfoDialog;
import com.yummy.hotchat.WifiTool.WifiTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.yummy.hotchat.Global.GetFilePathByUri.getPathByUri4kitkat;


public class Chat extends Fragment implements SocketServer.SConnectListener, SocketClient.ConnectListener {

    private View view;
    private RecyclerView rvChatList;
    private ChatAdapter chatAdapter;
    private EditText et;
    private TextView tvSend;
    private TextView tvFile;
    private String content;
    private WifiTools wifiTools;

    private SocketClient socketClient;

    private SocketServer socketServer;

    private final static String TAG = "Chat_Fragment";
    private int sign;

    private SocketUser socketUser; //用户的信息


    /**
     * 初始化WifiTool参数
     */
    private void initParameter() {
        wifiTools = WifiTools.getInstance(getActivity());
        MainActivity mainActivity = (MainActivity) getActivity();
        if (sign == ConstantC.SERVER_SIGN) {
            socketServer = mainActivity.getSocketServer();
            socketServer.setOnSConnectListener(this);
        } else {
            socketClient = mainActivity.getSocketClient();
            socketClient.setOnConnectListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.i(TAG, "CHAT FRAGMENT CREATED");
        initParameter();

        view = inflater.inflate(R.layout.fragment_chat, container, false);
        rvChatList = (RecyclerView) view.findViewById(R.id.recylerView);
        et = (EditText) view.findViewById(R.id.et);
        tvSend = (TextView) view.findViewById(R.id.tvSend);
        tvFile = (TextView) view.findViewById(R.id.tvFile);
        rvChatList.setHasFixedSize(true);
        rvChatList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvChatList.setAdapter(chatAdapter = new ChatAdapter());
        initData();


        return view;
    }

    /**
     * 初始化一些控件
     */
    private void initData() {

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                content = s.toString().trim();
                if (content.length() < 1) {
                    content = " ";
                }
            }
        });

        tvSend.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        tvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        });

        chatAdapter.setOnChatModelItemClickListener(new ChatAdapter.OnChatModelItemClickListener() {
            @Override
            public void onIconClick(ChatModel model) {
                UserInfoDialog userInfoDialog=new UserInfoDialog(getActivity(),getActivity().getLayoutInflater(),R.layout.dialog_user_info,model);
                userInfoDialog.initParameterAndShow();
            }

        });


    }

    /**
     * 用于调用客户端或服务器发送消息并更新界面的函数
     */
    void sendMessage() {
        try {
            if (sign == ConstantC.CLIENT_SIGN) {
                Log.i(TAG, "发送消息给服务器");
                if (wifiTools.getConnectedHotIP().size() >= 2) {
                    if (socketClient != null) {
                        socketClient.sendMessageToServer(socketUser.toString() + content, MessageType.CLIENT_MESSAGE_SIGN);
                    } else {
                        Log.i(TAG, "socketClient为null");
                        ToastMaker.toastSomething(getActivity(), "请确认你是否连接聊天室");
                    }
                } else {
                    Log.i(TAG, ConstantC.NO_USERS_TIPS);
                    ToastMaker.toastSomething(getActivity(), ConstantC.NO_USERS_TIPS);
                    return;
                }
            } else {
                if (wifiTools.getConnectedHotIP().size() >= 2) {
                    Log.i(TAG, "发送消息给客户端");
                    if (socketServer != null) {
                        socketServer.sendMessageToClient(socketUser.toString() + content, MessageType.SERVER_MESSAGE_SIGN);
                    } else {
                        Log.i(TAG, "socketServer为null");
                        ToastMaker.toastSomething(getActivity(), "请确认你是否连接聊天室");
                    }
                } else {
                    Log.i(TAG, ConstantC.NO_USERS_TIPS);
                    ToastMaker.toastSomething(getActivity(), ConstantC.NO_USERS_TIPS);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ArrayList<ItemModel> data = new ArrayList<>();
//        ChatModel model = new ChatModel();
//        model.setIcon("http://img.my.csdn.net/uploads/201508/05/1438760758_6667.jpg");
//        model.setContent(content);
//        data.add(new ItemModel(ItemModel.CHAT_B, model));
//        chatAdapter.addAll(data);
        rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);
        et.setText("");
        hideKeyBorad(et);
    }

    /**
     * 用于调用客户端或服务器发送文件的函数
     */
    void sendFile() {
        if (sign == ConstantC.CLIENT_SIGN) {
            if (wifiTools.getConnectedHotIP().size() >= 2) {
                Log.i(TAG, "发送消息给服务器");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, ConstantC.CHAT_CLIENT_FILE_PICKED);
            } else {
                Log.i(TAG, ConstantC.NO_USERS_TIPS);
                ToastMaker.toastSomething(getActivity(), ConstantC.NO_USERS_TIPS);
            }
        } else {
            if (wifiTools.getConnectedHotIP().size() >= 2) {
                Log.i(TAG, "发送文件给客户端");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, ConstantC.CHAT_SERVER_FILE_PICKED);
            } else {
                Log.i(TAG, ConstantC.NO_USERS_TIPS);
                ToastMaker.toastSomething(getActivity(), ConstantC.NO_USERS_TIPS);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case ConstantC.CHAT_SERVER_FILE_PICKED:
                if (resultCode == Activity.RESULT_OK) {

                    Uri uri = data.getData();  //得到uri，后面将uri转化成file的过程
                    File file = new File(getPathByUri4kitkat(getActivity(), uri));
//                    Toast.makeText(getActivity(), file.toString(), Toast.LENGTH_SHORT).show();
                    if (file.length() > 5242880) {
                        ToastMaker.toastSomething(getActivity(), "文件大小不得大于5MB!");
                        break;
                    }
                    try {
                        socketServer.sendFile(socketUser.getSign() + "", file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e(TAG, "file_pick_error");
                }
                break;

            case ConstantC.CHAT_CLIENT_FILE_PICKED:
                if (resultCode == Activity.RESULT_OK) {

                    Uri uri = data.getData();  //得到uri，后面将uri转化成file的过程
                    File file = new File(getPathByUri4kitkat(getActivity(), uri));
                    if (file.length() > 5242880) {
                        ToastMaker.toastSomething(getActivity(), "文件大小不得大于5MB!");
                        break;
                    }

                    try {
                        socketClient.sendFile(socketUser.toString(), file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "file_pick_error");
                }

                break;

            default:
                break;
        }
    }

    /**
     * 用于隐藏当前页面的键盘
     * @param v
     */
    private void hideKeyBorad(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    /**
     * 设置是服务器还是客户端的标记
     * @param sign
     */
    public void setSign(int sign) {
        this.sign = sign;
    }

    /**
     * 服务器接受到消息并进行处理
     * @param msg
     */
    @Override
    public void onSReceiveData(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                Log.i(TAG, "设置服务器监听收到消息" + msg);
                ArrayList<ItemModel> data = new ArrayList<>();
                ChatModel model = new ChatModel();


                String userSign = msg.substring(0, MessageType.ID_LEN);
                Log.i(TAG, "该消息由用户" + userSign + "发送");

                String[] infoMsg = msg.substring(MessageType.ID_LEN).split("/");

                String rMsg = infoMsg[5];
                Log.i(TAG, "消息内容为" + rMsg);

                model.setNick(infoMsg[0]);
                model.setIllustration(infoMsg[1]);
                model.setSex(infoMsg[2]);
                model.setIcon(infoMsg[3]);
                model.setTime(GetCurrentTime.getTime());
                model.setContent(rMsg);
                data.add(new ItemModel(ItemModel.CHAT_A, model));
                chatAdapter.addAll(data);
                chatAdapter.notifyDataSetChanged();
                rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);

            }
        });

    }

    /**
     * 服务器显示自己发送的消息
     * @param msg 待处理的消息
     */
    @Override
    public void onSNotify(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sign == ConstantC.SERVER_SIGN) {
                    Log.i(TAG, "设置服务器给出通知" + msg);
                    ArrayList<ItemModel> data = new ArrayList<>();
                    ChatModel model = new ChatModel();

                    String[] infoMsg = msg.substring(MessageType.MS_LEN).split("/");
                    String rMsg = infoMsg[5];
                    Log.i(TAG, "消息内容为" + rMsg);

                    model.setIcon(socketUser.getIcon());
                    model.setNick(socketUser.getUserName());
                    model.setIllustration(socketUser.getUserIllustration());
                    model.setSex(socketUser.getIsMan());
                    model.setTime(GetCurrentTime.getTime());
                    model.setContent(rMsg);
                    data.add(new ItemModel(ItemModel.CHAT_B, model));
                    chatAdapter.addAll(data);
                    chatAdapter.notifyDataSetChanged();
                    rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            }
        });

    }

    /**
     * 服务端检测到客户端连接的接口
     */
    @Override
    public void onClientConnected() {
        ToastMaker.toastSomething(getActivity(), ConstantC.USER_CONNECTED);
    }

    /**
     * 客户端处理数据并显示
     * @param msg 待处理的消息
     * @param type 消息类型
     */
    @Override
    public void onReceiveData(final String msg, final String type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case MessageType.CLIENT_MESSAGE_SIGN:
                        if (sign == ConstantC.CLIENT_SIGN) {
                            Log.i(TAG, "客户端监听收到消息" + msg);
                            String user_sign = msg.substring(0, MessageType.ID_LEN);
                            String msg2 = msg.substring(MessageType.ID_LEN);
                            Log.i(TAG, "客户端监修改后消息" + msg2);
                            Log.i(TAG, "本机标志为" + socketUser.getSign());
                            Log.i(TAG, "消息标志为" + user_sign);
                            if (!user_sign.equals(socketUser.getSign() + "")) {
                                Log.i(TAG, "该消息由用户" + user_sign + "发送");
                                ArrayList<ItemModel> data = new ArrayList<>();
                                ChatModel model = new ChatModel();

                                String[] infoMsg = msg.substring(MessageType.ID_LEN).split("/");
                                String rMsg = infoMsg[5];
                                Log.i(TAG, "消息内容为" + rMsg);

                                model.setNick(infoMsg[0]);
                                model.setIllustration(infoMsg[1]);
                                model.setSex(infoMsg[2]);
                                model.setIcon(infoMsg[3]);
                                model.setContent(rMsg);
                                model.setTime(GetCurrentTime.getTime());
                                data.add(new ItemModel(ItemModel.CHAT_A, model));

                                chatAdapter.addAll(data);
                                chatAdapter.notifyDataSetChanged();
                                rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);
                            } else {
                                Log.i(TAG, "该消息由用户自己发生");
                            }
                        }
                        break;
                    case MessageType.SERVER_MESSAGE_SIGN:
                        if (sign == ConstantC.CLIENT_SIGN) {
                            Log.i(TAG, "客户端监听收到消息" + msg);
                            Log.i(TAG, "该消息由用户服务器发送");
                            ArrayList<ItemModel> data = new ArrayList<>();
                            ChatModel model = new ChatModel();

                            String[] infoMsg = msg.substring(MessageType.MS_LEN).split("/");
                            String rMsg = infoMsg[5];
                            Log.i(TAG, "消息内容为" + rMsg);

                            model.setNick(infoMsg[0]);
                            model.setIllustration(infoMsg[1]);
                            model.setSex(infoMsg[2]);
                            model.setIcon(infoMsg[3]);
                            model.setTime(GetCurrentTime.getTime());
                            model.setContent(rMsg);
                            data.add(new ItemModel(ItemModel.CHAT_A, model));

                            chatAdapter.addAll(data);
                            chatAdapter.notifyDataSetChanged();
                            rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);

                        }
                        break;
                    case MessageType.SERVER_FILE_RECEIVED_SIGN:
                        if (sign == ConstantC.CLIENT_SIGN) {
                            Log.i(TAG, "客户端监听收到消息" + msg);
                            String user_sign = msg.substring(0, MessageType.ID_LEN);
                            String msg2 = msg.substring(MessageType.ID_LEN);
                            Log.i(TAG, "客户端监修改后消息" + msg2);
                            Log.i(TAG, "本机标志为" + socketUser.getSign());
                            Log.i(TAG, "消息标志为" + user_sign);
                            if (user_sign.equals(socketUser.getSign() + "")) {
                                Log.i(TAG, "该消息服务器接受到文件");
                                ArrayList<ItemModel> data = new ArrayList<>();
                                ChatModel model = new ChatModel();

                                String[] infoMsg = msg.substring(MessageType.ID_LEN).split("/");
                                String rMsg = infoMsg[5];
                                Log.i(TAG, "消息内容为" + rMsg);

                                model.setNick(infoMsg[0]);
                                model.setIllustration(infoMsg[1]);
                                model.setSex(infoMsg[2]);
                                model.setIcon(infoMsg[3]);
                                model.setTime(GetCurrentTime.getTime());
                                model.setContent(rMsg);
                                data.add(new ItemModel(ItemModel.CHAT_A, model));

                                chatAdapter.addAll(data);
                                chatAdapter.notifyDataSetChanged();
                                rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);
                            } else {
                                Log.i(TAG, "该消息服务器接受到文件,但不是发给自己的");
                            }
                        }


                        break;
                    default:
                        Log.i(TAG, "收到了未知消息");
                        break;
                }

            }
        });

    }

    /**
     * 客户端显示自己发送的消息
     * @param msg 待处理的消息
     */
    @Override
    public void onNotify(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sign == ConstantC.CLIENT_SIGN) {

                    Log.i(TAG, "设置客户端给出通知" + msg);

                    ArrayList<ItemModel> data = new ArrayList<>();
                    ChatModel model = new ChatModel();

                    String[] infoMsg = msg.substring(MessageType.MS_LEN + MessageType.ID_LEN).split("/");
                    String rMsg = infoMsg[5];
                    Log.i(TAG, "消息内容为" + rMsg);

                    model.setIcon(socketUser.getIcon());
                    model.setNick(socketUser.getUserName());
                    model.setIllustration(socketUser.getUserIllustration());
                    model.setSex(socketUser.getIsMan());
                    model.setTime(GetCurrentTime.getTime());
                    model.setContent(rMsg);

                    data.add(new ItemModel(ItemModel.CHAT_B, model));
                    chatAdapter.addAll(data);
                    chatAdapter.notifyDataSetChanged();
                    rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            }
        });
    }

    public void setSocketUser(SocketUser socketUser) {
        this.socketUser = socketUser;
    }

}
