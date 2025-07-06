package com.rhythmcoderzzf.androidstudysystem.storage;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;

import java.util.ArrayList;
import java.util.List;

public class StorageMainActivity extends BaseActivity {
    private final List<TitleBean> mList = new ArrayList<>();

    private List<TitleBean> initRvDataList() {
        mList.add(new TitleBean("应用专属存储", "应用专属存储的空间", getString(R.string.storage_app_specific_files_info), StorageAppSpecificActivity.class));
        mList.add(new TitleBean("SharedPreference", "", "偏好存储", StorageSharedPreferenceActivity.class));
        return mList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }
}