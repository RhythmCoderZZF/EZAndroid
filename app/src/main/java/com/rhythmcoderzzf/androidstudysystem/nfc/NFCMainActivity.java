package com.rhythmcoderzzf.androidstudysystem.nfc;

import android.os.Bundle;

import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;
import com.rhythmcoderzzf.androidstudysystem.R;

import java.util.ArrayList;
import java.util.List;

public class NFCMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean(getString(R.string.nfc_ndef), NFCTagActivity.class));
        list.add(new TitleBean(getString(R.string.nfc_tech), NFCTechActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }
}