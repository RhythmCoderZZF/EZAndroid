package com.rhythmcoderzzf.androidstudysystem.media;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.camera.CameraIntentActivity;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

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