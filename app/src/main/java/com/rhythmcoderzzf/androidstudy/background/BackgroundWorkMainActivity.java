package com.rhythmcoderzzf.androidstudy.background;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudy.R;
import com.rhythmcoderzzf.androidstudy.background.alarm.BackgroundAlarmActivity;
import com.rhythmcoderzzf.androidstudy.background.service.BackgroundServiceActivity;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class BackgroundWorkMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean(getString(R.string.background_service), BackgroundServiceActivity.class));
        list.add(new TitleBean(getString(R.string.background_alarm), BackgroundAlarmActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}