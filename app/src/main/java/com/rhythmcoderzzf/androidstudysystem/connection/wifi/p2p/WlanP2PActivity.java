package com.rhythmcoderzzf.androidstudysystem.connection.wifi.p2p;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.connection.wifi.p2p.wifidirect.WiFiDirectMgr;
import com.rhythmcoderzzf.androidstudysystem.connection.wifi.p2p.wifidirect.WifiDirectConnectInfo;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.cmd.CmdUtil;

import java.util.ArrayList;
import java.util.List;


public class WlanP2PActivity extends BaseActivity implements View.OnClickListener, WiFiDirectMgr.OnWifiDirectListener {
    private WiFiDirectMgr mP2pManager;

    private P2pDeviceAdapter mAdapter;

    private Button mBtnClient;
    private Server server;
    private Client client;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_p2p);
        mBtnClient = findViewById(R.id.btn_client_send);
        mBtnClient.setOnClickListener(this);
        RecyclerView recyclerView = findViewById(R.id.rv_devices);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new P2pDeviceAdapter(new ArrayList<>(), deviceName -> {
            mP2pManager.connectDevice(deviceName);
        });
        boolean isDeviceAsServer = !Build.MODEL.equals("23013RK75C");
        if (isDeviceAsServer) {
            findViewById(R.id.ll_wifi_devices).setVisibility(View.GONE);
        }
        recyclerView.setAdapter(mAdapter);
        mP2pManager = new WiFiDirectMgr(this, this);
        mP2pManager.start(isDeviceAsServer);
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
            if (server != null) {
                server.closeServer();
                server = null;
            }
            if (client != null) {
                client.closeClient();
                client = null;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_client_send) {
            if (client != null) {
                client.sendMessage("你好:" + SystemClock.uptimeMillis());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mP2pManager.stop();
    }
}