package com.yummy.hotchat.Socket;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yummy.hotchat.Global.ConstantC;
import com.yummy.hotchat.Global.MessageType;
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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by qq985 on 2016/11/23.
 */

public class SocketServer {


    static final String TAG = "SocketServer";
    private SConnectListener mListener;
    Socket socket;

    private SocketUser socketUser;

    private ServerHandler handler = new ServerHandler();
    private ServerSocket serverSocket;

    /**
     * 用来保存不同的客户端
     */
//    private static Map<String, Socket> socketClients = new LinkedHashMap<>();
    private static ArrayList<Socket> socketClients = new ArrayList<>();

    public void createServer() {

        new ServerListener().run();

    }

    /**
     * 释放端口
     *
     * @throws IOException
     */
    public void closeSocket() throws IOException {
        Log.i(TAG,"施放端口");
        if (serverSocket != null) {
            serverSocket.close();
            serverSocket=new ServerSocket();
        } else {

        }
    }

    /**
     * 监听客户端连接的线程
     */
    class ServerListener extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(4000);
                serverSocket = new ServerSocket(12345);//默认端口为12345
                while (true) { // 由于可能当有多个客户端连接时，accept方法就会产生多个Socket对象，需加一个while循环监听来自客户端的连接
                    socket = serverSocket.accept();// 侦听事务的连接，accept是一个阻塞的方法，会阻塞当前的主线程，并且返回的是一个Socket类型
                    // 建立连接，表示SocketServer在监听，如果监听到有客户端连接则会调用accept方法，然后返回一个Socket，最后建立连接
                    // 对于每一个客户端开启一个线程进行处理
                    new HandleThread(socket).start();
                    socketClients.add(socket);
                    Log.i(TAG, "有客户端连接到了本机的12345端口");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理来自服务器的消息
     *
     * @param data 未经处理的消息
     * @throws IOException
     */
    private void handleMessageFromClient(String data) throws IOException {
        switch (data.substring(0, MessageType.MS_LEN)) {

            case MessageType.CLIENT_MESSAGE_SIGN:
                data = data.substring(MessageType.MS_LEN);
                mListener.onSReceiveData(data);
                Log.i(TAG, "from client:" + data);
                Log.i(TAG, "转发给其他客户端");
                sendMessageToClient(data, MessageType.CLIENT_MESSAGE_SIGN);
                break;
            case MessageType.CLIENT_USER_ID_SIGN:
//                data = data.substring(MessageType.MS_LEN);
//                Log.i(TAG, "收到用户信息：" + data);
//                String[] userData = data.split("/");
//                socketClients.put(userData[3], socket);
                break;
            default:
                Log.e(TAG, "收到了未知信息，错误！" + data);
                break;
        }

    }

    class ServerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantC.CLIENT_CONNECTED:
                    mListener.onClientConnected();
                    break;
                default:
                    break;
            }
        }
    }

    class HandleThread extends Thread {

        private Socket socket;

        public HandleThread(Socket socket) {
            this.socket = socket;

        }

        @Override
        public void run() {
            try {

                Log.i(TAG, "新客户端连接");
                Message msg = new Message();
                msg.what = ConstantC.CLIENT_CONNECTED;
                handler.sendMessage(msg);

                //读取从客户端发送过来的数据
                InputStream inputStream = socket.getInputStream();
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println(inetAddress.getHostAddress());

                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1) {
//                    System.out.println("ASDADSA");
                    String data = new String(buffer, 0, len);

                    if (mListener != null) {

                        if (data.startsWith(MessageType.CLIENT_FILE_SIGN)) {
                            try {
                                Log.i(TAG, "Client File:" + data);
                                getFileFromClient(data, inputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            handleMessageFromClient(data);
                        }
                    } else {
                        Log.i(TAG, "server_null");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 发送消息给客户端
     *
     * @param msg  消息内容
     * @param type 消息类型
     * @throws IOException
     */
    public void sendMessageToClient(String msg, String type) throws IOException {

        ArrayList<Socket> badSocket = new ArrayList<>();

        switch (type) {
            case MessageType.CLIENT_MESSAGE_SIGN:
                msg = type + msg;
                for (Socket socket : socketClients) {
                    Log.i(TAG, "给一个客户端发送了消息：" + msg);
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(msg.getBytes("utf-8"));
                        outputStream.flush();
                        //socket.shutdownOutput();
//                        outputStream.close();
                    } catch (Exception e) {
                        Log.i(TAG, "pipe broken??");
                        socket.getOutputStream().flush();
                        e.printStackTrace();
                        badSocket.add(socket);
                        continue;
                    }

                }

                for (Socket socket : badSocket) {
                    socketClients.remove(socket);
                    Log.i(TAG, "删除了一个破旧的管道1");
                }

                break;
            case MessageType.SERVER_MESSAGE_SIGN:
                msg = type + msg;
                if (mListener != null) {
                    mListener.onSNotify(msg);
                } else {
                    Log.i(TAG, "服务器自己显示消息出错");
                }

                for (Socket socket : socketClients) {
                    try {
                        Log.i(TAG, "给一个客户端发送了消息：" + msg);
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(msg.getBytes("utf-8"));
                        outputStream.flush();
                        //socket.shutdownOutput();
//                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        socket.getOutputStream().flush();
                        Log.i(TAG, "pipe broken??");
                        badSocket.add(socket);
                        continue;
                    }
                }

                for (Socket socket : badSocket) {
                    socketClients.remove(socket);
                    Log.i(TAG, "删除了一个破旧的管道2");
                }

                break;

            case MessageType.SERVER_FILE_RECEIVED_SIGN:

                msg = type + msg;
                for (Socket socket : socketClients) {
                    try {
                        Log.i(TAG, "给一个客户端发送了消息：" + msg);
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(msg.getBytes("utf-8"));
                        outputStream.flush();
                        //socket.shutdownOutput();
//                    outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        socket.getOutputStream().flush();
                        Log.i(TAG, "pipe broken??");
                        badSocket.add(socket);
                        continue;
                    }

                }

                for (Socket socket : badSocket) {
                    socketClients.remove(socket);
                    Log.i(TAG, "删除了一个破旧的管道3");
                }

                break;

            default:

                for (Socket socket : badSocket) {
                    socketClients.remove(socket);
                    Log.i(TAG, "删除了一个破旧的管道4");
                }
                socket.getOutputStream().flush();
                //socket.shutdownOutput();

                break;
        }
//        msg = type + msg;
//        for (Map.Entry socket : socketClients.entrySet()) {
//
//            Log.i(TAG, "给一个客户端发送了消息：" + msg);
//            OutputStream outputStream = ((Socket) (socket.getValue())).getOutputStream();
//            outputStream.write(msg.getBytes("utf-8"));
//
//        }


    }

    /**
     * 发送文件给客户端
     *
     * @param msg  用户信息
     * @param file 文件
     */
    public void sendFile(String msg, File file) throws IOException {

        ArrayList<Socket> badSocket = new ArrayList<>();

        Log.i(TAG, "File Name:" + file.getName());
        Log.i(TAG, "msg:" + msg);
        for (Socket socket : socketClients) {
            try {
                Log.i(TAG, "给一个客户端发送了文件消息：" + MessageType.SERVER_FILE_SIGN + msg);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write((MessageType.SERVER_FILE_SIGN + msg + socketUser.toString() + "文件" + file.getName() + "已由服务器发送~").getBytes("utf-8"));
                outputStream.flush();
                //socket.shutdownOutput();

                new SendFileToClient(socket, file).start();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "pipe broken??");
                socket.getOutputStream().flush();
                //socket.shutdownOutput();
                continue;
            }

        }

        for (Socket socket : badSocket) {
            socketClients.remove(socket);
            Log.i(TAG, "删除了一个破旧的管道5");
        }
//        for (Map.Entry socket : socketClients.entrySet()) {
//            new SendFileToClient((Socket) socket.getValue(), file).start();
//        }

    }

    /**
     * 发送文件给客户端的线程
     */
    class SendFileToClient extends Thread {

        Socket socket;
        File file;

        public SendFileToClient(Socket socket, File file) {
            this.socket = socket;
            this.file = file;
        }

        @Override
        public void run() {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                Log.i(TAG, "发送文件给客户端");
                FileInputStream fileInputStream = new FileInputStream(file);

                DataInputStream fis = new DataInputStream(new BufferedInputStream(fileInputStream));

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                socket.getOutputStream().flush();
                dos.writeUTF(file.getName());
                dos.flush();
                long file_size = file.length();
                Log.i(TAG, "文件长度：" + file_size / 1024 + "KB");
                dos.writeLong(file_size);
                dos.flush();

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
                        break;
                    }
                    dos.write(buf, 0, read);
                    dos.flush();
                }

                dos.flush();
                fis.close();
                //socket.shutdownOutput();

                Log.i(TAG, "已经发出");
                if (mListener != null) {

                    mListener.onSNotify(MessageType.SERVER_FILE_SIGN + socketUser.toString() + "文件：" + file.getName() + "已发送~");

                } else {
                    Log.i(TAG, "已经发出通知出错");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 接受从客户端传来的文件
     *
     * @throws IOException
     */
    public void getFileFromClient(String userData, InputStream inputStream) throws IOException {


        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
//        InputStream inputStream = socket.getInputStream();

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
        long_len = dataInputStream.readLong();

//        int readName = dataInputStream.read(buf);
//        String str = new String(buf, 0, readName);
//        savePath += str;
//        int readSize = dataInputStream.read(buf);
//        String str2 = new String(buf, 0, readSize);
//        long_len = Long.parseLong(str2);

//        Log.i(TAG, "name:" + str + "----size" + long_len);


        File file = new File(savePath);


        mListener.onSReceiveData(userData.substring(MessageType.MS_LEN));

        DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(file))));


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
            String userSign = userData.substring(MessageType.MS_LEN, MessageType.MS_LEN + MessageType.ID_LEN);
            Log.i(TAG, "userSign：" + userSign);
//            sendMessageToClient(userData.substring(MessageType.MS_LEN, userData.lastIndexOf("/") + 1) + "文件：" + file.getName() + "已被服务器接受~", MessageType.SERVER_FILE_RECEIVED_SIGN);
            sendMessageToClient(userSign + socketUser.toString() + "文件：" + file.getName() + "已被服务器接受~", MessageType.SERVER_FILE_RECEIVED_SIGN);

            mListener.onSNotify(userSign + MessageType.SERVER_FILE_SIGN + socketUser.toString() + "文件：" + file.getName() + "服务器已接受~");
//            mListener.onSReceiveData("文件：" + file.getName() + "已接受~");
            Log.i(TAG, "已转发文件给客户端");
//            sendMessageToClient(userData.split("/")[4] + "文件：" + file.getName() + "已转发~", MessageType.SERVER_MESSAGE_SIGN);
            Log.i(TAG, "userData" + userData);

            //转发给其他客户端的执行
            sendFile(userData.substring(MessageType.MS_LEN, MessageType.ID_LEN + MessageType.MS_LEN), file);
        } else {
            Log.i(TAG, "已经发出通知出错");
        }
    }

    /**
     * 从主ac传来
     *
     * @param socketUser
     */
    public void setSocketUser(SocketUser socketUser) {
        this.socketUser = socketUser;
    }

    /**
     * 设置监听器函数
     *
     * @param linstener
     */
    public void setOnSConnectListener(SConnectListener linstener) {
        this.mListener = linstener;
    }


    /**
     * 数据接收回调接口
     */
    public interface SConnectListener {
        void onSReceiveData(String data);

        void onSNotify(String msg);

        void onClientConnected();
    }

}



