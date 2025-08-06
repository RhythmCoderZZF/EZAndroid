package com.rhythmcoderzzf.androidstudysystem.connection.wifi.p2p;

import com.rhythmcoderzzf.baselib.cmd.CmdUtil;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private String TAG = "Client";
    private final String address;
    private final int port;
    private Socket socket;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void sendMessage(String string) {
        new Thread(() -> {
            BufferedWriter writer = null;
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(address, port)), 1000);
                CmdUtil.d(TAG, "Client starting send to:" + address + ":" + port + " socket isConnected:" + socket.isConnected());
                OutputStream output = socket.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(output));
                writer.write(string);
                CmdUtil.d(TAG, "Sent to server: " + string);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
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

