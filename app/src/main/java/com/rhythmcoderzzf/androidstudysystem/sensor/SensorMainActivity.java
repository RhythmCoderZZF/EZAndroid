package com.rhythmcoderzzf.androidstudysystem.sensor;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.sensor.vibrator.VibratorActivity;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class SensorMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean("基本使用", SensorBaseActivity.class));
        list.add(new TitleBean("震动", VibratorActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}