package com.rhythmcoderzzf.androidstudysystem.background;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.background.alarm.BackgroundAlarmActivity;
import com.rhythmcoderzzf.androidstudysystem.background.service.BackgroundServiceActivity;
import com.rhythmcoderzzf.androidstudysystem.background.service.fgservice.BackgroundForegroundServiceActivity;
import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityBackgroundAlarmBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class BackgroundWorkMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean(getString(R.string.background_service), BackgroundServiceActivity.class));
        list.add(new TitleBean(getString(R.string.background_fg_service), BackgroundForegroundServiceActivity.class));
        list.add(new TitleBean(getString(R.string.background_bind_service), BackgroundForegroundServiceActivity.class));
        list.add(new TitleBean(getString(R.string.background_alarm), BackgroundAlarmActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

    @Override
    protected ViewBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityBackgroundAlarmBinding.inflate(layoutInflater);
    }

}