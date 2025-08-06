package com.rhythmcoderzzf.androidstudysystem.connection.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityBluetoothBaseBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.connection.EZBluetooth;
import com.rhythmcoderzzf.ezandroid.permission.EZPermission;

@SuppressLint("MissingPermission")
public class BTBaseActivity extends BaseActivity<ActivityBluetoothBaseBinding> {

    private EZBluetooth ezBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.btnOpenBt.setOnClickListener(this::btnOpenBt);
        EZPermission ezPermission = new EZPermission.Builder(this)
                .applyRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT)
                .build();
        ezPermission.requestPermission(null);
        ezBluetooth = new EZBluetooth.Builder(this)
                .setOnBluetoothCallback(new MyOnBluetoothCallback())
                .build();

    }


    @Override
    protected ActivityBluetoothBaseBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityBluetoothBaseBinding.inflate(layoutInflater);
    }

    private void btnOpenBt(View view) {
        ezBluetooth.open();
    }

    static class MyOnBluetoothCallback extends EZBluetooth.OnBluetoothCallback {

    }
}