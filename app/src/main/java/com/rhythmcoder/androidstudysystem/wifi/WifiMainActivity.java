package com.rhythmcoder.androidstudysystem.wifi;

import android.os.Bundle;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.wifi.p2p.WlanP2PActivity;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.ListAdapter;
import com.rhythmcoder.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class WifiMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean("WLAN 扫描", "获取设备上可见的 WLAN 接入点列表。", "", WlanScanActivity.class));
        list.add(new TitleBean("WLAN P2P", "可让具有相应硬件的设备通过 Wi-Fi 直接相互连接，而无需中间接入点",
                "Wi-Fi 直连（点对点）可让具有相应硬件的设备通过 Wi-Fi 直接相互连接，而无需中间接入点。借助这些 API，您可以发现并连接到其他设备（当每台设备支持 Wi-Fi 点对点时），然后通过高速连接进行通信，这些距离远远超过蓝牙连接。这对于在用户之间共享数据的应用非常有用，例如多人游戏或照片共享应用。",
                WlanP2PActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}