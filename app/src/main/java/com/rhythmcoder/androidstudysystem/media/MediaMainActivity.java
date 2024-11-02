package com.rhythmcoder.androidstudysystem.media;

import android.os.Bundle;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.media.camera.CameraIntentActivity;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.ListAdapter;
import com.rhythmcoder.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class MediaMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean("录屏", "使用MediaProjection简单实现录屏", "", MediaProjectionSimpleActivity.class));
        list.add(new TitleBean("录屏+录音", "MediaProjection + MediaRecord实现录屏录音", "", MediaProjectionRecordActivity.class));
        list.add(new TitleBean("Camera", "使用Intent操作默认相机应用", "", CameraIntentActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}