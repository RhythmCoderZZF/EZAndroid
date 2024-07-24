package com.rhythmcoder.androidstudysystem.ui_views;

import android.os.Bundle;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.androidstudysystem.ui_views.notification.UIViewsNotificationActivity;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.ListAdapter;
import com.rhythmcoder.baselib.TitleBean;

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