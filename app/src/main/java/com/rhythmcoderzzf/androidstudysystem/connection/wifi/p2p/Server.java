package com.rhythmcoderzzf.androidstudysystem.connection.wifi.p2p;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final String TAG = "Server";
    private Thread mthread;

    private OnReceiveListener mOnReceiveListener;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private volatile boolean mIsShutdown = false;

    public Server() {
    }

    public Server setOnReceiveListener(OnReceiveListener mOnReceiveListener) {
        this.mOnReceiveListener = mOnReceiveListener;
        return this;
    }

    interface OnReceiveListener {
        void onReceive(String string);

        void onDisConnect();
    }


    public void start() {
        if (serverSocket != null) {
            return;
        }
        mthread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(8988));
                while (!mIsShutdown) {
                    Log.d(TAG, "Server is running and waiting for client...");
                    clientSocket = serverSocket.accept();
                    Log.d(TAG, "Client connected: " + clientSocket);
                    InputStream input = clientSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String line = "";
                    String appendLine = "";
                    while ((appendLine = reader.readLine()) != null) {
                        line += appendLine;
                    }
                    mOnReceiveListener.onReceive(line);
                    Log.d(TAG, "clientSocket over receive String:" + line);
                    reader.close();
                    clientSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        mthread.start();
    }

    public void closeServer() {
        mIsShutdown = true;
        try {
            serverSocket.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

