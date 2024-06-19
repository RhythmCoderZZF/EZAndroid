package com.rhythmcoder.androidstudysystem.wifi.p2p;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.util.ArrayList;
import java.util.List;


//【1初始化】1.在AndroidManifest中，请求在设备上使用 Wi-Fi 硬件的权限
public class WlanP2PActivity extends BaseActivity implements View.OnClickListener {
    private WifiP2pManager mP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mWiFiDirectBroadcastReceiver;
    private IntentFilter mIntentFilter;

    private WifiP2pDevice mP2pDevice;
    private WifiP2pInfo mWifiP2pInfo;
    private P2pDeviceAdapter mAdapter;


    private Button mBtnServer;
    private Button mBtnClient;

    /**
     * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
     */
    private class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //【1初始化】2.检查 WLAN 点对点连接是否已开启且受支持，向您的 activity 通知 Wi-Fi 点对点状态，并相应地做出反应
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                CmdUtil.d(TAG, "receive:WIFI_P2P_ST`ATE_CHANGED_ACTION");
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    CmdUtil.d(TAG, "Wifi P2P is enabled");
                } else {
                    CmdUtil.e(TAG, "Wi-Fi P2P is not enabled");
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                CmdUtil.d(TAG, "receive:WIFI_P2P_PEERS_CHANGED_ACTION");
                //【2发现对等设备】2.如果发现过程成功并检测到对等设备，系统会广播 WIFI_P2P_PEERS_CHANGED_ACTION intent
                if (mP2pManager != null) {
                    //【2发现对等设备】3.使用 requestPeers() 请求已发现对等设备的列表
                    mP2pManager.requestPeers(mChannel, wifiP2pDeviceList -> {
                        if (wifiP2pDeviceList != null && !wifiP2pDeviceList.getDeviceList().isEmpty()) {
                            mAdapter.setDataList((new ArrayList<>(wifiP2pDeviceList.getDeviceList())));
                            mAdapter.notifyDataSetChanged();
                        } else {
                            toast("没有发现可用设备");
                        }
                    });
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                CmdUtil.d(TAG, "receive:WIFI_P2P_CONNECTION_CHANGED_ACTION");

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    mP2pManager.requestConnectionInfo(mChannel, info -> {
                        mWifiP2pInfo = info;
                    });
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                CmdUtil.d(TAG, "receive:WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_p2p);
        mBtnServer = findViewById(R.id.btn_server_start);
        mBtnServer.setOnClickListener(this);
        mBtnClient = findViewById(R.id.btn_client_send);
        mBtnClient.setOnClickListener(this);
        findViewById(R.id.btn_discoverPeers).setOnClickListener(this);
        //【1初始化】3.向 Wi-Fi 点对点框架注册您的应用,得到一个Channel，它用于将应用连接到 WLAN 点对点连接框架
        mP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mP2pManager.initialize(this, getMainLooper(), null);
        mWiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver();

        //【1初始化】4.创建一个 intent 过滤器
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        RecyclerView recyclerView = findViewById(R.id.rv_devices);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new P2pDeviceAdapter(new ArrayList<>(), device -> {
            mP2pDevice = device;
            //【3连接对等设备】
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            mP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    //success logic
                    toast("mP2pManager.connect SUCCESS!");
                }

                @Override
                public void onFailure(int reason) {
                    //failure logic
                    toast("mP2pManager.connect Failure:" + reason);
                }
            });
        });
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //【1初始化】5.注册广播
        registerReceiver(mWiFiDirectBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //【1初始化】6.注销广播
        unregisterReceiver(mWiFiDirectBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_discoverPeers) {
            //【2发现对等设备】1.调用 discoverPeers() 以检测范围内且可用于连接的可用对等设备。
            mP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    //onSuccess() 方法只会通知您发现过程已成功，而不会提供它发现的实际对等设备（如果有）的任何信息
                    toast("成功发现对等设备");
                }

                @Override
                public void onFailure(int reasonCode) {
                    toast("发现对等设备失败:" + reasonCode);
                }
            });
        } else if (v.getId() == R.id.btn_server_start) {
            new Server().setOnReceiveListener(new Server.OnReceiveListener() {
                @Override
                public void onReceive(String string) {
                    toast(string);
                }

                @Override
                public void onDisConnect() {
                    toast("断开连接~");
                }
            }).start();
        } else if (v.getId() == R.id.btn_client_send) {
            new Client(mWifiP2pInfo.groupOwnerAddress.getHostAddress(), 8888).sendMessage("你好:" + SystemClock.uptimeMillis());
        }

    }


    private void showInContextUI() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Attention");
        dialog.setMessage("We need permission to Continue the action or workflow in this app !!");
        dialog.setNegativeButton("cancel", (d, which) -> {
            d.dismiss();
        });
        dialog.setPositiveButton("ok", (dialog1, which) -> {
            dialog1.dismiss();

        });
        dialog.show();
    }
}