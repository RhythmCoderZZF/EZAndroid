package com.rhythmcoderzzf.ezandroid.connection;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.rhythmcoderzzf.ezandroid.utils.EZLogUtil;

import java.util.Objects;

/**
 * wifi模块相关
 */
public class EZWifi implements DefaultLifecycleObserver {
    private static final String TAG = EZWifi.class.getSimpleName();
    public Context mContext;
    private final WifiChangeReceiver mWifiReceiver;
    private final WifiManager mWifiManager;
    private final Callback mCallback;

    private class WifiChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EZLogUtil.v(TAG, "receive broadcast intent action:" + intent.getAction());
            if (Objects.equals(intent.getAction(), WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (mCallback != null) {
                        mCallback.onWifiConnected(mWifiManager.getConnectionInfo(), getCurrentConnectedSSID());
                    }
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

    public EZWifi register(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(this);
        return this;
    }

    @Override
    public final void onCreate(@NonNull LifecycleOwner owner) {
        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mWifiReceiver, intentFilter);
    }

    @Override
    public final void onDestroy(@NonNull LifecycleOwner owner) {
        mContext.unregisterReceiver(mWifiReceiver);
        owner.getLifecycle().removeObserver(this);
    }

    /**
     * 获取当前连接的wifi ssid
     *
     * @return wifi ssid
     */
    public String getCurrentConnectedSSID() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        if (ssid.equalsIgnoreCase("<unknown ssid>")) {
            ssid = "";
        }
        return ssid;
    }

    public static class Callback {
        protected void onWifiConnected(WifiInfo info, String ssid) {
        }
    }
}