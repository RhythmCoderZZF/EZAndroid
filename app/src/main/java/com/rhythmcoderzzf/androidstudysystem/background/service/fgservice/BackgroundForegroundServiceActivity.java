package com.rhythmcoderzzf.androidstudysystem.background.service.fgservice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;

public class BackgroundForegroundServiceActivity extends BaseActivity implements View.OnClickListener {
    private Intent mIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_forground_service);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        mIntent = new Intent(this, ForegroundService.class);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            //startService(mIntent); 该方法也可以
            startForegroundService(mIntent);
        } else if (v.getId() == R.id.btn_stop) {
            stopService(mIntent);
        }
    }
}
