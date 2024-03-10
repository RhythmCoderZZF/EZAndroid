package com.rhythmcoder.androidstudysystem.background.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.Nullable;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;

public class BackgroundServiceActivity extends BaseActivity implements View.OnClickListener {
    private Intent mIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_service);
        findViewById(R.id.btn_start_service).setOnClickListener(this);
        findViewById(R.id.btn_stop_service).setOnClickListener(this);
        mIntent = new Intent(this, BackgroundService.class);
        mIntent.putExtra("id", 10010);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start_service) {
            startService(mIntent);
        } else if (v.getId() == R.id.btn_stop_service) {
            stopService(mIntent);
        }
    }
}
