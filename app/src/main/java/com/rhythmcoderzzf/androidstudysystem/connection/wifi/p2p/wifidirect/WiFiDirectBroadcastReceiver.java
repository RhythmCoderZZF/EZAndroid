package com.rhythmcoderzzf.androidstudysystem.connection.wifi.p2p.wifidirect;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "WiFiDirectMgr.WiFiP2PDirectBroadcastReceiver";
    private WifiP2pManager manager;
    private Channel channel;
    private final WifiP2pBroadcastReceiverListener wifiP2pBroadcastReceiverListener;
    private IntentFilter mIntentFilter;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WifiP2pBroadcastReceiverListener wifiP2pBroadcastReceiverListener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.wifiP2pBroadcastReceiverListener = wifiP2pBroadcastReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //在设备上启用或停用 Wi-Fi 点对点连接时广播。
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(TAG, "broadcast onReceive Wifi-P2P state changed state:" + state);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                wifiP2pBroadcastReceiverListener.setWifiP2pEnabled(true);
            } else {
                wifiP2pBroadcastReceiverListener.setWifiP2pEnabled(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //【2发现对等设备】2.在你调用 discoverPeers() 时广播。如果您在应用中处理此 intent，通常需要调用 requestPeers() 来获取更新后的对等设备列表。
            Log.d(TAG, "broadcast onReceive Wifi-P2P peers changed");
            if (manager != null) {
                manager.requestPeers(channel, wifiP2pBroadcastReceiverListener::onPeersAvailable);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //【3连接对等设备】2.当设备的 Wi-Fi 连接状态更改时广播。应用可以使用 requestConnectionInfo()、requestNetworkInfo() 或 requestGroupInfo() 检索当前连接信息。
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            boolean isNetworkConnected = networkInfo.isConnected();
            Log.d(TAG, "broadcast onReceive Wifi-P2P CONNECTION changed isNetworkConnected:"+isNetworkConnected);
            if (isNetworkConnected) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, wifiP2pBroadcastReceiverListener::onConnectionInfoAvailable);
            } else {
                wifiP2pBroadcastReceiverListener.onSessionConnected(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //当设备的详细信息（例如设备名称）发生更改时广播。检应用可以使用 requestDeviceInfo() 检索当前连接信息。
            Log.d(TAG, "broadcast onReceive Wifi-P2P THIS_DEVICE changed");
            wifiP2pBroadcastReceiverListener.updateThisP2pDevice(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

    public IntentFilter getIntentFilter() {
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        }
        return mIntentFilter;
    }

    interface WifiP2pBroadcastReceiverListener {
        void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList);

        void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);

        void setWifiP2pEnabled(boolean enabled);

        void updateThisP2pDevice(WifiP2pDevice wifiP2pDevice);

        void onSessionConnected(boolean isConnected);
    }

    public static String getDeviceStatus(WifiP2pDevice wifiP2pDevice) {
        int deviceStatus = wifiP2pDevice.status;
        String status = "Unknown";
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                status = "Available";
                break;
            case WifiP2pDevice.INVITED:
                status = "Invited";
                break;
            case WifiP2pDevice.CONNECTED:
                status = "Connected";
                break;
            case WifiP2pDevice.FAILED:
                status = "Failed";
                break;
            case WifiP2pDevice.UNAVAILABLE:
                status = "Unavailable";
                break;
            default:
                status = "Unknown";
        }
        return status;
    }
}