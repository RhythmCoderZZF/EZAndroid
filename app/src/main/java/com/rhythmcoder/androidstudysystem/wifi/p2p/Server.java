package com.rhythmcoder.androidstudysystem.wifi.p2p;

import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final String TAG = "Server";
    private Thread mthread;

    private OnReceiveListener mOnReceiveListener;

    public Server setOnReceiveListener(OnReceiveListener mOnReceiveListener) {
        this.mOnReceiveListener = mOnReceiveListener;
        return this;
    }

    interface OnReceiveListener {
        void onReceive(String string);

        void onDisConnect();
    }


    public void start() {
        mthread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8888);
                CmdUtil.d(TAG, "Server is running and waiting for client...");

                // 等待客户端连接
                Socket clientSocket = serverSocket.accept();
                CmdUtil.d(TAG, "Client connected: " + clientSocket);

                // 获取输入流，用于从客户端接收数据
                InputStream input = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                // 获取输出流，用于向客户端发送数据
                // 循环接收和处理客户端的数据
                String line;
                while ((line = reader.readLine()) != null) {
                    CmdUtil.d(TAG, "Received String from client: " + line);

                    // 假设收到数据后进行简单的处理
                    mOnReceiveListener.onReceive(line);

                    // 如果接收到特定消息表示结束，可以退出循环
                    if ("bye!".equalsIgnoreCase(line)) {
                        CmdUtil.d(TAG, "disconnect from client: " + line);
                        mOnReceiveListener.onDisConnect();
                        break;
                    }
                }

                // 关闭连接
                serverSocket.close();
                CmdUtil.d(TAG, "Server closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        mthread.start();
    }
}

