package com.rhythmcoder.androidstudysystem.wifi.p2p;

import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

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
    }

    public void sendMessage(String string) {
        new Thread(() -> {
            BufferedWriter writer = null;
            BufferedReader reader = null;
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(address, port)), 5000);
                CmdUtil.d(TAG, "Client starting send to:" + address + ":" + port + " socket isConnected:" + socket.isConnected());
                OutputStream output = socket.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(output));
                InputStream input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));
                writer.write(string);
                CmdUtil.d(TAG, "Sent to server: " + string);

                // 接收服务器的响应
//                String response = reader.readLine();
//                CmdUtil.d(TAG, "Server response: " + response);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.close();
                    reader.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }).start();
    }

    public void closeClient() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

