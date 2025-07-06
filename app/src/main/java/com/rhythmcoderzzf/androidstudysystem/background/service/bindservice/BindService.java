package com.rhythmcoderzzf.androidstudysystem.background.service.bindservice;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.rhythmcoderzzf.androidstudysystem.background.service.BaseService;

import java.util.Random;

public class BindService extends BaseService {
    private final IBinder mBinder = new LocalBinder();
    private final Random mGenerator = new Random();

    class LocalBinder extends Binder {
        public BindService getService() {
            return BindService.this;
        }
    }

    public BindService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return mBinder;
    }

    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }
}