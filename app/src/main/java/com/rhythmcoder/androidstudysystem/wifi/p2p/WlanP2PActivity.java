package com.rhythmcoder.androidstudysystem.wifi.p2p;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.permission.PermissionUtil;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.util.ArrayList;


//【1初始化】1.在AndroidManifest中，请求在设备上使用 Wi-Fi 硬件的权限
public class WlanP2PActivity extends BaseActivity implements View.OnClickListener, WiFiDirectBroadcastReceiver.WifiP2pBroadcastReceiverListener {
    private WifiP2pManager mP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mWiFiDirectBroadcastReceiver;
    private IntentFilter mIntentFilter;

    private WifiP2pDevice mWifiP2pDevice;
    private WifiP2pInfo mWifiP2pInfo;
    private P2pDeviceAdapter mAdapter;

    private Button mBtnServer;
    private Button mBtnClient;

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
        if (!initP2p()) {
            finish();
        }
        new PermissionUtil(this, Manifest.permission.ACCESS_FINE_LOCATION).requestPermission();

        mWiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(mP2pManager, mChannel, this);
        //【1初始化】4.创建一个 intent 过滤器
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        RecyclerView recyclerView = findViewById(R.id.rv_devices);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new P2pDeviceAdapter(new ArrayList<>(), device -> {
            mWifiP2pDevice = device;
            //【3连接对等设备】
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            mP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
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

    private boolean initP2p() {
        // Device capability definition check
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            CmdUtil.e(TAG, "Wi-Fi Direct is not supported by this device.");
            return false;
        }
        // Hardware capability check
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            CmdUtil.e(TAG, "Cannot get Wi-Fi system service.");
            return false;
        }
        if (!wifiManager.isP2pSupported()) {
            CmdUtil.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }
        mP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mP2pManager == null) {
            CmdUtil.e(TAG, "Cannot get Wi-Fi Direct system service.");
            return false;
        }
        mChannel = mP2pManager.initialize(this, getMainLooper(), null);
        if (mChannel == null) {
            CmdUtil.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
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
                    runOnUiThread(() -> toast(string));

                }
                @Override
                public void onDisConnect() {
                    toast("断开连接~");
                }
            }).start();
        } else if (v.getId() == R.id.btn_client_send) {
            new Client(mWifiP2pInfo.groupOwnerAddress.getHostAddress(), 8988).sendMessage("你好:" + SystemClock.uptimeMillis());
        }

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        if (wifiP2pDeviceList != null && !wifiP2pDeviceList.getDeviceList().isEmpty()) {
            mAdapter.setDataList((new ArrayList<>(wifiP2pDeviceList.getDeviceList())));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        mP2pManager.requestConnectionInfo(mChannel, info -> {
            mWifiP2pInfo = info;
            CmdUtil.d(TAG, "wifip2pinfo:" + info);
        });
    }

    @Override
    public void needResetData() {
        mAdapter.setDataList((new ArrayList<>()));
        mAdapter.notifyDataSetChanged();
        mWifiP2pInfo = null;
    }

    @Override
    public void wifiP2pEnabled(boolean enabled) {

    }

    @Override
    public void thisWifiP2pDeviceChanged(WifiP2pDevice wifiP2pDevice) {

    }

}