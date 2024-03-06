package com.rhythmcoderzzf.androidstudy.storage;

import android.os.Bundle;

import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;
import com.rhythmcoderzzf.androidstudy.R;
import com.rhythmcoderzzf.androidstudy.storage.all.StorageAppSpecificActivity;

import java.util.ArrayList;
import java.util.List;

public class StorageMainActivity extends BaseActivity {
    private final List<TitleBean> mList = new ArrayList<>();

    private List<TitleBean> initRvDataList() {
        mList.add(new TitleBean(getString(R.string.storage_app_specific_files), getString(R.string.storage_app_specific_files_sub_title), getString(R.string.storage_app_specific_files_info), StorageAppSpecificActivity.class));
        return mList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        initSimpleProjectListView(findViewById(R.id.rv), new ListAdapter(initRvDataList()));
    }
}