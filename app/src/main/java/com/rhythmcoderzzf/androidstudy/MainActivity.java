package com.rhythmcoderzzf.androidstudy;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudy.background.BackgroundWorkMainActivity;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;
import com.rhythmcoderzzf.androidstudy.nfc.NFCMainActivity;
import com.rhythmcoderzzf.androidstudy.permission.PermissionMainActivity;
import com.rhythmcoderzzf.androidstudy.storage.StorageMainActivity;
import com.rhythmcoderzzf.androidstudy.usb.UsbMainActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView mRv;
    private final List<TitleBean> mList = new ArrayList<>();

    private void initRvDataList() {
        mList.add(new TitleBean("USB", getString(R.string.usb_sub_title), getString(R.string.usb_info), UsbMainActivity.class));
        mList.add(new TitleBean(getString(R.string.permission), getString(R.string.permission_sub_title), getString(R.string.permission_info), PermissionMainActivity.class));
        mList.add(new TitleBean("NFC", NFCMainActivity.class));
        mList.add(new TitleBean(getString(R.string.storage), "", getString(R.string.storage_info), StorageMainActivity.class));
        mList.add(new TitleBean(getString(R.string.background), BackgroundWorkMainActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRvDataList();
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new StaggeredGridLayoutManager(2, RecyclerView.VERTICAL));
        ListAdapter adapter = new ListAdapter(mList);
        mRv.setAdapter(adapter);
    }
}