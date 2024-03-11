package com.rhythmcoder.androidstudysystem.background.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.rhythmcoder.baselib.cmd.CmdUtil;

public class BaseService extends Service {

    protected static final String TAG = "Service";

    @Override
    public void onCreate() {
        super.onCreate();
        CmdUtil.i(TAG, "onCreate<<");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        CmdUtil.i(TAG, "onBind<<");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        CmdUtil.i(TAG, "onUnbind<<");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CmdUtil.i(TAG, "onStartCommand<<");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        CmdUtil.i(TAG, "onDestroy<<");
        super.onDestroy();
    }
}
