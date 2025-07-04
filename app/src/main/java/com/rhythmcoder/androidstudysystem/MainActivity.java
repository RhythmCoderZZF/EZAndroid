package com.rhythmcoder.androidstudysystem;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;

import com.rhythmcoder.androidstudysystem.background.BackgroundWorkMainActivity;
import com.rhythmcoder.androidstudysystem.camera.CameraMainActivity;
import com.rhythmcoder.androidstudysystem.media.MediaMainActivity;
import com.rhythmcoder.androidstudysystem.other.OtherMainActivity;
import com.rhythmcoder.androidstudysystem.sensor.SensorMainActivity;
import com.rhythmcoder.androidstudysystem.ui.UIMainActivity;
import com.rhythmcoder.androidstudysystem.ui_views.UIViewsMainActivity;
import com.rhythmcoder.androidstudysystem.wifi.WifiMainActivity;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.ListAdapter;
import com.rhythmcoder.baselib.TitleBean;
import com.rhythmcoder.androidstudysystem.nfc.NFCMainActivity;
import com.rhythmcoder.androidstudysystem.permission.PermissionMainActivity;
import com.rhythmcoder.androidstudysystem.storage.StorageMainActivity;
import com.rhythmcoder.androidstudysystem.usb.UsbMainActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView mRv;
    private final List<TitleBean> mList = new ArrayList<>();

    private void initRvDataList() {
        mList.add(new TitleBean("其他","杂项", "", OtherMainActivity.class));
        mList.add(new TitleBean("USB", getString(R.string.usb_sub_title), getString(R.string.usb_info), UsbMainActivity.class));
        mList.add(new TitleBean(getString(R.string.permission), getString(R.string.permission_sub_title), getString(R.string.permission_info), PermissionMainActivity.class));
        mList.add(new TitleBean("NFC", NFCMainActivity.class));
        mList.add(new TitleBean(getString(R.string.storage), "", getString(R.string.storage_info), StorageMainActivity.class));
        mList.add(new TitleBean(getString(R.string.background), BackgroundWorkMainActivity.class));
        mList.add(new TitleBean("WIFI", WifiMainActivity.class));
        mList.add(new TitleBean("Sensor", SensorMainActivity.class));
        mList.add(new TitleBean("UI", UIMainActivity.class));
        mList.add(new TitleBean("UI-Views", UIViewsMainActivity.class));
        mList.add(new TitleBean("多媒体(Media)","","", MediaMainActivity.class));
        mList.add(new TitleBean("相机(Camera)","Intent启动相机","", CameraMainActivity.class));
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