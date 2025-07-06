package com.rhythmcoderzzf.androidstudysystem.wifi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.connection.EZWifi;

import java.util.List;

public class WlanScanActivity extends BaseActivity implements View.OnClickListener {
    private TextView mTv;
    private WifiManager mWifiManager;
    private String[] mPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    private TextView tvConnectedSSID;
    private EZWifi mEZWifiManager;

    private EZWifi.Callback mCallback = new EZWifi.Callback() {
        @Override
        protected void onWifiConnected(WifiInfo info, String ssid) {
            updateTvConnectedSSID(info, ssid);
        }
    };

    //1.系统会在完成扫描请求时调用此监听器，提供其成功/失败状态。
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            } else {
                // scan failure handling
                scanFailure();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_wlan_scan);
        mTv = findViewById(R.id.tv_result);
        findViewById(R.id.btn_scan).setOnClickListener(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, intentFilter);

        tvConnectedSSID = findViewById(R.id.tv_ssid);
        mEZWifiManager = EZWifi.getInstance(this, mCallback).register(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_scan) {
            if (ContextCompat.checkSelfPermission(this, mPermissions[0]) == PackageManager.PERMISSION_GRANTED) {
                boolean success = mWifiManager.startScan();
                if (!success) {
                    scanFailure();
                }
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, mPermissions[0])) {
                showInContextUI();
            } else {
                requestPermissions(mPermissions, 0);
            }
        }
    }

    private void scanFailure() {
        toast("扫描失败");
    }

    private void scanSuccess() {
        List<ScanResult> results = mWifiManager.getScanResults();
        mTv.setText("");
        results.forEach(scanResult -> {
            mTv.append("ssid:" + scanResult.SSID + " level:" + scanResult.level + " frequency:" + scanResult.frequency + "\n");
        });
    }

    private void showInContextUI() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Attention");
        dialog.setMessage("We need permission to Continue the action or workflow in this app !!");
        dialog.setNegativeButton("cancel", (d, which) -> {
            d.dismiss();
        });
        dialog.setPositiveButton("ok", (dialog1, which) -> {
            dialog1.dismiss();
            requestPermissions(mPermissions, 0);

        });
        dialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toast("已开启相关权限");
                } else {
                    if (shouldShowRequestPermissionRationale(permissions[0])) {
                        toast("需要开启相关权限才能使用该功能哦");
                    } else {
                        toast("权限被禁用");
                    }
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void updateTvConnectedSSID(WifiInfo info, String ssid) {
        tvConnectedSSID.setText(ssid);
    }
}