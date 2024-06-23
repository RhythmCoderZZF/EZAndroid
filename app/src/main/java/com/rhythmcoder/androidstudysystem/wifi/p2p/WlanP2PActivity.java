package com.rhythmcoder.androidstudysystem.wifi.p2p;

import static com.rhythmcoder.androidstudysystem.wifi.p2p.WiFiDirectBroadcastReceiver.getDeviceStatus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    private WifiP2pInfo mWifiP2pInfo;
    private P2pDeviceAdapter mAdapter;

    private Button mBtnClient;
    private Button mBtnRemoveGroup;
    private TextView mTvP2pDevice;
    private TextView mTvP2pInfo;
    private TextView mTvP2pGroupInfo;
    private Boolean isWifiP2pEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_p2p);
        mBtnClient = findViewById(R.id.btn_client_send);
        mBtnClient.setOnClickListener(this);
        findViewById(R.id.btn_discoverPeers).setOnClickListener(this);
        mTvP2pDevice = findViewById(R.id.tv_p2p_device);
        mTvP2pInfo = findViewById(R.id.tv_p2p_info);
        mTvP2pGroupInfo = findViewById(R.id.tv_group_info);
        mBtnRemoveGroup = findViewById(R.id.btn_remove_group);
        mBtnRemoveGroup.setOnClickListener(this);
        //【1初始化】3.向 Wi-Fi 点对点框架注册您的应用,得到一个Channel，它用于将应用连接到 WLAN 点对点连接框架
        if (!initP2p()) {
            finish();
        }
        new PermissionUtil(this, Manifest.permission.ACCESS_FINE_LOCATION).requestPermission();

        mWiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(mP2pManager, mChannel, this);
        //【1初始化】4.创建一个 intent 过滤器

        RecyclerView recyclerView = findViewById(R.id.rv_devices);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new P2pDeviceAdapter(new ArrayList<>(), device -> {
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
        mChannel = mP2pManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                toast("解散wifip2p群组，请重新进入Activity");
            }
        });
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
        registerReceiver(mWiFiDirectBroadcastReceiver, mWiFiDirectBroadcastReceiver.getIntentFilter());
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
                    toast("成功发现一些对等设备");
                }

                @Override
                public void onFailure(int reasonCode) {
                    toast("发现一些对等设备失败:" + reasonCode);
                }
            });
        } else if (v.getId() == R.id.btn_client_send) {
            new Client(mWifiP2pInfo.groupOwnerAddress.getHostAddress(), 8988).sendMessage("你好:" + SystemClock.uptimeMillis());
        } else if (v.getId() == R.id.btn_remove_group) {
            mP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    toast("移除失败 Reason:" + reasonCode);

                }

                @Override
                public void onSuccess() {
                    toast("移除成功");
                }
            });
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
        mWifiP2pInfo = wifiP2pInfo;
        mTvP2pInfo.setText("WifiP2pInfo:\n");
        mTvP2pInfo.append(wifiP2pInfo.toString());

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            findViewById(R.id.tv_server).setVisibility(View.VISIBLE);
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
        } else if (wifiP2pInfo.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            findViewById(R.id.ll_client).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
        mTvP2pGroupInfo.setText("WifiP2pGroup信息:\n");
        mTvP2pGroupInfo.append(wifiP2pGroup.toString());
    }

    @Override
    public void resetData() {
        mAdapter.setDataList((new ArrayList<>()));
        mAdapter.notifyDataSetChanged();
        mTvP2pDevice.setText("WifiP2pDevice信息:\n");
        mTvP2pGroupInfo.setText("WifiP2pGroup信息:\n");
    }

    @Override
    public void setWifiP2pEnabled(boolean enabled) {
        this.isWifiP2pEnabled = enabled;
    }

    @Override
    public void updateThisP2pDevice(WifiP2pDevice wifiP2pDevice) {
        mAdapter.setDataList((new ArrayList<>()));
        mAdapter.notifyDataSetChanged();
        mTvP2pDevice.setText("WifiP2pDevice信息:\n");
        mTvP2pDevice.append(wifiP2pDevice.toString());
        String status = getDeviceStatus(wifiP2pDevice);
        mTvP2pDevice.append(" status:" + status);
    }
}