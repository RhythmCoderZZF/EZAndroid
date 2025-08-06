package com.rhythmcoderzzf.androidstudysystem.connection.wifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityWifiWlanScanBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.permission.EZPermission;
import com.rhythmcoderzzf.ezandroid.connection.EZWifi;

import java.util.List;

@SuppressLint("MissingPermission")
public class WlanScanActivity extends BaseActivity<ActivityWifiWlanScanBinding> implements View.OnClickListener {
    private EZWifi mEZWifi;

    private EZWifi.Callback mCallback = new EZWifi.Callback() {
        @Override
        protected void onWifiConnected(WifiInfo info) {
            updateTvConnectedSSID(info);
        }

        protected void onScanResult(List<ScanResult> scanResults) {
            updateScanResult(scanResults);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.btnScan.setOnClickListener(this);
        mEZWifi = EZWifi.getInstance(this, mCallback).registerReceiver(WifiManager.NETWORK_STATE_CHANGED_ACTION
                , WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }

    @Override
    protected ActivityWifiWlanScanBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityWifiWlanScanBinding.inflate(layoutInflater);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_scan) {
            new EZPermission.Builder(this).applyRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.CHANGE_WIFI_STATE).build().requestPermission((granted, deniedPermissions) -> {
                if (granted && !mEZWifi.startScan()) {
                    toast("请打开Wifi");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEZWifi.unRegisterReceiver();
    }

    private void updateTvConnectedSSID(WifiInfo info) {
        mBinding.tvSsid.setText(info.getSSID());
    }

    private void updateScanResult(List<ScanResult> scanResults) {
        mBinding.tvResult.setText("");
        scanResults.forEach(scanResult -> {
            mBinding.tvResult.append("ssid:" + scanResult.SSID + " level:" + scanResult.level + " frequency:" + scanResult.frequency + "\n");
        });
    }
}