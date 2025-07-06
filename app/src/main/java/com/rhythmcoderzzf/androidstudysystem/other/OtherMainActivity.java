package com.rhythmcoderzzf.androidstudysystem.other;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.other.phone.PhoneMainActivity;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class OtherMainActivity extends BaseActivity {
    private final List<TitleBean> mList = new ArrayList<>();

    private List<TitleBean> initRvDataList() {
        mList.add(new TitleBean("Info", "设备信息", "", ADBShellActivity.class));
        mList.add(new TitleBean("电话", "电话相关", "", PhoneMainActivity.class));
        return mList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }
}