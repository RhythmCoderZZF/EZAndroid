package com.rhythmcoderzzf.androidstudysystem.camera;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class CameraMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean("Intent调用相机", "", "", CameraIntentActivity.class));
        list.add(new TitleBean("Camera2基本使用", "", "", Camera2APIActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}