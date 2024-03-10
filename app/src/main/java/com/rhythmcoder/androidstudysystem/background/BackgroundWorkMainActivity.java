package com.rhythmcoder.androidstudysystem.background;

import android.os.Bundle;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.background.alarm.BackgroundAlarmActivity;
import com.rhythmcoder.androidstudysystem.background.service.BackgroundServiceActivity;
import com.rhythmcoder.androidstudysystem.background.service.fgservice.BackgroundForegroundServiceActivity;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.ListAdapter;
import com.rhythmcoder.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class BackgroundWorkMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean(getString(R.string.background_service), BackgroundServiceActivity.class));
        list.add(new TitleBean(getString(R.string.background_fg_service), BackgroundForegroundServiceActivity.class));
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