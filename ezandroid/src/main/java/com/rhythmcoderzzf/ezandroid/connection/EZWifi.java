package com.rhythmcoderzzf.ezandroid.connection;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.rhythmcoderzzf.ezandroid.utils.EZLogUtil;

import java.util.List;

/**
 * wifi模块相关,需要权限——Manifest.permission.ACCESS_WIFI_STATE;ACCESS_FINE_LOCATION
 */
@SuppressLint("MissingPermission")
public class EZWifi {
    private static final String TAG = EZWifi.class.getSimpleName();
    public Context mContext;
    private final WifiChangeReceiver mWifiReceiver;
    private final WifiManager mWifiManager;
    private final Callback mCallback;

    private class WifiChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EZLogUtil.v(TAG, "receive broadcast intent action:" + intent.getAction());
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (mCallback != null) {
                        mCallback.onWifiConnected(mWifiManager.getConnectionInfo());
                    }
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (!success) {
                    return;
                }
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                if (scanResults == null || scanResults.isEmpty()) {
                    return;
                }
                if (mCallback != null) {
                    mCallback.onScanResult(scanResults);
                }
            }
        }
    }

    private EZWifi(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiChangeReceiver();
    }

    public static EZWifi getInstance(Context context, Callback callback) {
        return new EZWifi(context, callback);
    }

    public EZWifi registerReceiver(String... actions) {
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actions) {
            intentFilter.addAction(action);
        }
        mContext.registerReceiver(mWifiReceiver, intentFilter);
        return this;
    }

    public EZWifi unRegisterReceiver() {
        if (mWifiReceiver != null) mContext.unregisterReceiver(mWifiReceiver);
        return this;
    }

    /**
     * 扫描附近WiFi
     */
    public boolean startScan() {
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            mWifiManager.startScan();
            return true;
        }
        return false;
    }

    /**
     * 回调类
     */
    public static class Callback {
        /**
         * 当WIFI连接成功回调
         *
         * @param info 已连接的wifi。例如""wifi-01"",如果为空则返回""<unknown ssid>""。注意getSsid()获取的WiFi名带双引号
         */
        protected void onWifiConnected(WifiInfo info) {
        }

        /**
         * 扫描附近的wifi列表
         * @param scanResults 附近的wifi列表。注意getWifiSsid()获取的WiFi名没有双引号
         */
        protected void onScanResult(List<ScanResult> scanResults) {

        }
    }
}