package com.rhythmcoderzzf.androidstudysystem.ui_views;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.ui_views.notification.UIViewsNotificationActivity;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class UIViewsMainActivity extends BaseActivity {
    private List<TitleBean> initRvDataList() {
        List<TitleBean> list = new ArrayList<>();
        list.add(new TitleBean("Notification","通知相关的知识","", UIViewsNotificationActivity.class));
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }

}