package com.rhythmcoder.androidstudysystem.background.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BackgroundService extends BaseService {
    private static final String TAG = BackgroundService.class.getSimpleName();
    private int mCount = 0;
    private Thread mBackgroundThread;
    private int mStartId = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        CmdUtil.i(TAG, "onStartCommand<< flags:" + flags + " startId:" + startId);
        if (mBackgroundThread == null || !mBackgroundThread.isAlive()) {
            mBackgroundThread = new Thread(() -> {
                mCount = 0;
                while (mCount < 5 && !Thread.interrupted()) {
                    try {
                        mCount++;
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        CmdUtil.e(TAG, "interrupt Work Thread");
                    }
                    CmdUtil.d(TAG, SimpleDateFormat.getDateInstance().format(new Date()) + "--" + mCount);
                }
                stopSelf(mStartId);
            });
            mBackgroundThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBackgroundThread.interrupt();
    }
}