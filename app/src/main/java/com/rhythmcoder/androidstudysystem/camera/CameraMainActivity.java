package com.rhythmcoder.androidstudysystem.camera;

import android.os.Bundle;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.media.MediaProjectionRecordActivity;
import com.rhythmcoder.androidstudysystem.media.MediaProjectionSimpleActivity;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.ListAdapter;
import com.rhythmcoder.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class CameraMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean("Camera", "通过Intent启动相机", "", CameraIntentActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}