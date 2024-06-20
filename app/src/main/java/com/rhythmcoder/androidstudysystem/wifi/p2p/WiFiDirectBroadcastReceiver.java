package com.rhythmcoder.androidstudysystem.wifi.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import com.rhythmcoder.baselib.cmd.CmdUtil;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "WiFiDirectBroadcastReceiver";
    private WifiP2pManager manager;
    private Channel channel;
    private final WifiP2pBroadcastReceiverListener wifiP2pBroadcastReceiverListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WifiP2pBroadcastReceiverListener wifiP2pBroadcastReceiverListener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.wifiP2pBroadcastReceiverListener = wifiP2pBroadcastReceiverListener;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //【1初始化】2.检查 WLAN 点对点连接是否已开启且受支持，向您的 activity 通知 Wi-Fi 点对点状态，并相应地做出反应
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            CmdUtil.d(TAG, "onReceive P2P state changed - " + state);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                wifiP2pBroadcastReceiverListener.wifiP2pEnabled(true);
            } else {
                wifiP2pBroadcastReceiverListener.wifiP2pEnabled(false);
                wifiP2pBroadcastReceiverListener.needResetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //【2发现对等设备】2.如果发现过程成功并检测到对等设备，系统会广播 WIFI_P2P_PEERS_CHANGED_ACTION intent
            CmdUtil.d(TAG, "onReceive P2P peers changed");
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, wifiP2pBroadcastReceiverListener::onPeersAvailable);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            CmdUtil.d(TAG, "onReceive P2P CONNECTION changed");
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, wifiP2pBroadcastReceiverListener::onConnectionInfoAvailable);
            } else {
                // It's a disconnect
                wifiP2pBroadcastReceiverListener.needResetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            CmdUtil.d(TAG, "onReceive P2P THIS_DEVICE changed");
            wifiP2pBroadcastReceiverListener.thisWifiP2pDeviceChanged(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

    interface WifiP2pBroadcastReceiverListener {
        void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList);

        void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);

        void needResetData();

        void wifiP2pEnabled(boolean enabled);

        void thisWifiP2pDeviceChanged(WifiP2pDevice wifiP2pDevice);
    }
}