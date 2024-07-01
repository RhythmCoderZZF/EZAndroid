package com.rhythmcoder.androidstudysystem.wifi.p2p;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.wifi.p2p.wifidirect.P2pDeviceAdapter;
import com.rhythmcoder.androidstudysystem.wifi.p2p.wifidirect.WiFiDirectMgr;
import com.rhythmcoder.androidstudysystem.wifi.p2p.wifidirect.WifiDirectConnectInfo;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.util.ArrayList;
import java.util.List;


public class WlanP2PActivity extends BaseActivity implements View.OnClickListener, WiFiDirectMgr.OnWifiDirectListener {
    private WiFiDirectMgr mP2pManager;

    private P2pDeviceAdapter mAdapter;

    private Button mBtnClient;
    private Button mBtnRemoveGroup;
    private Server server;
    private Client client;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_p2p);
        mBtnClient = findViewById(R.id.btn_client_send);
        mBtnClient.setOnClickListener(this);
        mBtnRemoveGroup = findViewById(R.id.btn_remove_group);
        mBtnRemoveGroup.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.rv_devices);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new P2pDeviceAdapter(new ArrayList<>(), deviceName -> {
            mP2pManager.connectDevice(deviceName);
        });
        recyclerView.setAdapter(mAdapter);
        mP2pManager = new WiFiDirectMgr(this, this);
        mP2pManager.start(!Build.MODEL.equals("23013RK75C"));
    }


    @Override
    public void onDevicePeersDiscovered(List<String> deviceNames) {
        if (deviceNames == null) {
            deviceNames = new ArrayList<>();
        }
        mAdapter.setDataList(deviceNames);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSessionConnected(boolean isConnected) {
        if (isConnected) {
            WifiDirectConnectInfo info = mP2pManager.getmWifiDirectConnectInfo();
            if (info != null) {
                CmdUtil.d(TAG, "WifiDirectConnectInfo:" + info);
                if (info.isGroupFormed && info.isGroupOwner) {
                    findViewById(R.id.tv_server).setVisibility(View.VISIBLE);
                    server = new Server();
                    server.setOnReceiveListener(new Server.OnReceiveListener() {
                        @Override
                        public void onReceive(String string) {
                            toast(string);
                        }

                        @Override
                        public void onDisConnect() {
                            toast("断开连接~");
                        }
                    }).start();
                } else if (info.isGroupFormed) {
                    // The other device acts as the client. In this case, we enable the
                    // get file button.
                    findViewById(R.id.ll_client).setVisibility(View.VISIBLE);
                    if (client == null) {
                        client = new Client(info.groupOwnerAddress.getHostAddress(), 8988);
                    }
                }
            }
        } else {
//            if (server1 != null) {
//                server1.closeServer();
//            }
//            if (client1 != null) {
//                client1.closeClient();
//            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_client_send) {
            client.sendMessage("你好:" + SystemClock.uptimeMillis());
        } else if (v.getId() == R.id.btn_remove_group) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mP2pManager.stop();
        if (server != null) server.closeServer();
        if (client != null) client.closeClient();
    }
}