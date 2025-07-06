package com.rhythmcoderzzf.androidstudysystem;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;

import com.rhythmcoderzzf.androidstudysystem.background.BackgroundWorkMainActivity;
import com.rhythmcoderzzf.androidstudysystem.camera.CameraMainActivity;
import com.rhythmcoderzzf.androidstudysystem.media.MediaMainActivity;
import com.rhythmcoderzzf.androidstudysystem.other.OtherMainActivity;
import com.rhythmcoderzzf.androidstudysystem.sensor.SensorMainActivity;
import com.rhythmcoderzzf.androidstudysystem.ui.UIMainActivity;
import com.rhythmcoderzzf.androidstudysystem.ui_views.UIViewsMainActivity;
import com.rhythmcoderzzf.androidstudysystem.wifi.WifiMainActivity;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.ListAdapter;
import com.rhythmcoderzzf.baselib.TitleBean;
import com.rhythmcoderzzf.androidstudysystem.nfc.NFCMainActivity;
import com.rhythmcoderzzf.androidstudysystem.permission.PermissionMainActivity;
import com.rhythmcoderzzf.androidstudysystem.storage.StorageMainActivity;
import com.rhythmcoderzzf.androidstudysystem.usb.UsbMainActivity;

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