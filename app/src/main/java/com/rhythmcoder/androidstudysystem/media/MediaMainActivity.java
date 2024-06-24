package com.rhythmcoder.androidstudysystem.media;

import android.os.Bundle;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.wifi.WlanScanActivity;
import com.rhythmcoder.androidstudysystem.wifi.p2p.WlanP2PActivity;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.ListAdapter;
import com.rhythmcoder.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class MediaMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean("录屏", "MediaProjection实现录屏", "", MediaProjectionActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}