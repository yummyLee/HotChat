package com.yummy.hotchat.Socket;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.yummy.hotchat.Global.ConstantC;
import com.yummy.hotchat.Global.MessageType;
import com.yummy.hotchat.Global.ToastMaker;
import com.yummy.hotchat.Object.SocketUser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by yummy on 2016/11/23.
 */

public class SocketClient {

    Socket socket;
    static final String TAG = "SocketClient";
    private ConnectListener mListener;

    private SocketUser socketUser;

    public void createClient(final String ip) {
        new Thread() {
            @Override
            public void run() {
                try {

                    socket = new Socket(ip, 12345);
//                    socket.setSoTimeout();
                    Log.i(TAG, "已经连接上了服务器");

//                    //先发送用户标志
//                    sendMessageToServer(socketUser.getSign()+"",MessageType.CLIENT_USER_ID_SIGN);
//                    Log.i(TAG,"发送用户标志给服务器:"+socketUser.getSign());
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = -1;

                    while ((len = inputStream.read(buffer)) != -1) {
                        String data = new String(buffer, 0, len);

                        //通过回调接口将获取到的数据推送出去
                        if (mListener != null) {
                            if (data.startsWith(MessageType.SERVER_FILE_SIGN)) {

                                Log.i(TAG, "收到的文件发送提示为" + data);
                                String sign = data.substring(MessageType.MS_LEN, MessageType.ID_LEN + MessageType.MS_LEN);

                                data = MessageType.SERVER_FILE_SIGN + data.substring(MessageType.MS_LEN + MessageType.ID_LEN);

                                mListener.onReceiveData(data, MessageType.SERVER_MESSAGE_SIGN);
                                Log.i(TAG, "该文件的发送者是" + sign);
                                if (sign.equals(socketUser.getSign() + "")) {
                                    Log.i(TAG, "该文件是自己的");
                                } else {
                                    Log.i(TAG, "收到了一个文件消息，开始接受文件");
//                                if ()
                                    try {
                                        getFileFromServer(data);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    socket.getOutputStream().flush();

                                }

                            } else {
                                Log.i(TAG, "收到准备处理的消息：" + data);
                                handleMessageFromServer(data);
                            }
                        } else {
                            System.out.println("null");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 处理来自服务器的生消息
     * @param data
     */
    private void handleMessageFromServer(String data) {
        switch (data.substring(0, MessageType.MS_LEN)) {
            case MessageType.SERVER_MESSAGE_SIGN:
                Log.i(TAG, "该消息由服务器发送");
                if (mListener != null) {
                    mListener.onReceiveData(data, MessageType.SERVER_MESSAGE_SIGN);
                }
                break;
            case MessageType.CLIENT_MESSAGE_SIGN:
                data = data.substring(MessageType.MS_LEN);
                String sign2 = data.substring(0, MessageType.ID_LEN);
                Log.i(TAG, "该消息由" + sign2 + "发送");
                if (mListener != null) {
                    mListener.onReceiveData(data, MessageType.CLIENT_MESSAGE_SIGN);
                }
                break;
            case MessageType.SERVER_FILE_RECEIVED_SIGN:
                data = data.substring(MessageType.MS_LEN);
                Log.i(TAG, "该消息由服务器收到文件确定后发送");
                if (mListener != null) {
                    mListener.onReceiveData(data, MessageType.SERVER_FILE_RECEIVED_SIGN);
                }
                break;
            default:
                Log.e(TAG, "收到了未知信息，错误！！");
                break;
        }
    }

    /**
     * 发送文件给服务器的线程
     */
    class SendFileToServer extends Thread {

        Socket socket;
        File file;

        public SendFileToServer(Socket socket, File file) {
            this.socket = socket;
            this.file = file;
        }

        @Override
        public void run() {

            try {
                Log.i(TAG, "发送文件给服务器");
                FileInputStream fileInputStream = new FileInputStream(file);

                DataInputStream fis = new DataInputStream(new BufferedInputStream(fileInputStream));

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//                OutputStream outputStream = socket.getOutputStream();
                dos.flush();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                dos.flush();

                dos.writeUTF(file.getName());
                long file_size = file.length();
                Log.i(TAG, "文件长度：" + file_size / 1024 + "KB");
                dos.writeLong(file_size);
                dos.flush();

//                dos.write(file.getName().getBytes());
//                dos.flush();
//                dos.write((file_size + "").getBytes());
//                dos.flush();

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                int bufferSize = 8192;
                byte[] buf = new byte[bufferSize];

                long pass_len = 0;

                while (true) {

                    int read = 0;
                    if (fis != null) {
                        read = fis.read(buf);
                        pass_len += read;
                    }

                    Log.i(TAG, "文件发出了" + (pass_len * 100 / file_size) + "%\n");

                    if (read == -1) {
                        Log.i(TAG, "read完毕");
                        break;
                    }

                    dos.write(buf, 0, read);
                    dos.flush();
//                    outputStream.write(buf);
                }

                dos.flush();
//                socket.shutdownOutput();
//                outputStream.flush();
                fis.close();

                Log.i(TAG, "已经发出");
                if (mListener != null) {
                    mListener.onNotify(MessageType.CLIENT_FILE_SIGN + socketUser.getSign() + socketUser.toString() + "文件：" + file.getName() + "已发送~");
//                    sendMessageToServer(socketUser.toString() + "文件：" + file.getName() + "已发送~", MessageType.CLIENT_FILE_SIGN);
                } else {
                    Log.i(TAG, "已经发出通知出错");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 接受从服务器传来的文件
     *
     * @throws IOException
     */
    public void getFileFromServer(String userData) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        // 本地保存路径，文件名会自动从服务器端继承而来。
        int bufferSize = 8192;
        byte[] buf = new byte[bufferSize];
        int passedlen = 0;
        long long_len = 0;

        String savePath = Environment.getExternalStorageDirectory().getPath() + "/HotChatFiles/";
        File fileDir = new File(savePath);
        if (!fileDir.exists()) {
            Log.i(TAG, "文件夹不存在，创建新文件夹");
            fileDir.mkdir();
        }

        savePath += dataInputStream.readUTF();
        File file = new File(savePath);

        DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(file))));
        long_len = dataInputStream.readLong();

        Log.i(TAG, "File Size()：" + long_len + "bytes");
        Log.i(TAG, "开始接收文件");
        long rest = 0;
        while (true) {

            int read = 0;
            if (dataInputStream != null) {
                read = dataInputStream.read(buf);
            }
            passedlen += read;
            System.out.println("read:" + read + "   pass:" + passedlen + "   sum:" + long_len);
            if (read == -1) {
                break;
            }
            // 下面进度条本为图形界面的prograssBar做的，这里如果是打文件，可能会重复打印出一些相同的百分比

            Log.i(TAG, "文件接收了" + (passedlen * 100 / long_len) + "%\n");

            if (rest == long_len - passedlen) {
                Log.i(TAG, "收到的文件不完整");
            }
            rest = long_len - passedlen;

            fileOut.write(buf, 0, read);
            fileOut.flush();

            if (passedlen == long_len) {
                break;
            } else if ((passedlen * 100 / long_len) == 100) {
                Log.i(TAG, "收到的文件不完整2");
                break;
            }
        }
        Log.i(TAG, "接收完成，文件存为" + savePath + "\n");

        socket.getOutputStream().flush();
        fileOut.flush();
        fileOut.close();

        if (mListener != null) {
            sendMessageToServer(socketUser.toString() + "文件：" + file.getName() + "已被客户端接受~", MessageType.CLIENT_MESSAGE_SIGN);
//            mListener.onNotify(s+"文件：" + file.getName() + "已接受~");
        } else {
            Log.i(TAG, "已经发出通知出错");
        }

    }

    /**
     * 发送消息给服务器
     *
     * @param msg 消息内容
     * @param type 消息类型
     * @throws IOException
     */
    public void sendMessageToServer(String msg, String type) throws IOException {

        Log.i(TAG, "发出消息函数");
        OutputStream outputStream = socket.getOutputStream();
        msg = type + socketUser.getSign() + msg;
        if (mListener != null) {
            if (!type.equals(MessageType.CLIENT_FILE_SIGN)) {
                mListener.onNotify(msg);
            }
        } else {
            Log.i(TAG, "客户端自己显示消息错误");
        }
        outputStream.write(msg.getBytes("utf-8"));
        outputStream.flush();

        Log.i(TAG, "发出消息" + msg);

    }

    /**
     * 发送文件给客户端
     *
     * @param file
     */
    public void sendFile(String userData, File file) {

        try {
//            sendMessageToServer(userData, MessageType.CLIENT_FILE_SIGN);
            sendMessageToServer(socketUser.toString() + "文件：" + file.getName() + "客户端已发送~", MessageType.CLIENT_FILE_SIGN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "File Name:" + file.getName());
        new SendFileToServer(socket, file).start();

    }


    public void setSocketUser(SocketUser socketUser) {
        this.socketUser = socketUser;
    }

    public void setOnConnectListener(ConnectListener listener) {
        this.mListener = listener;
    }

    /**
     * 数据接收回调接口
     */
    public interface ConnectListener {
        void onReceiveData(String msg, String type);

        void onNotify(String msg);
    }


}
