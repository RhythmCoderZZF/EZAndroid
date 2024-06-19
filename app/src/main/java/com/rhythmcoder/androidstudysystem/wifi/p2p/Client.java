package com.rhythmcoder.androidstudysystem.wifi.p2p;

import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private String TAG = "Client";
    private final String address;
    private final int port;
    private Socket socket;
    int len;
    byte buf[] = new byte[1024];

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
        socket = new Socket();
    }

    public void sendMessage(String string) {
        new Thread(() -> {
            BufferedWriter writer = null;
            BufferedReader reader = null;
            try {
                // 创建Socket连接到服务器
                socket = new Socket(address, port);
                CmdUtil.d(TAG, "Client starting send to:" + address + ":" + port);

                // 获取输出流，用于向服务器发送数据
                OutputStream output = socket.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(output));

                // 获取输入流，用于接收服务器的响应数据
                InputStream input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));
                // 向服务器发送数据
                writer.write(string);
                CmdUtil.d(TAG, "Sent to server: " + string);

                // 接收服务器的响应
                String response = reader.readLine();
                CmdUtil.d(TAG, "Server response: " + response);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.close();
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

