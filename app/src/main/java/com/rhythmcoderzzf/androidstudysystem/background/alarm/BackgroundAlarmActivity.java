package com.rhythmcoderzzf.androidstudysystem.background.alarm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityBackgroundAlarmBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;

public class BackgroundAlarmActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_alarm);
        findViewById(R.id.btn_alarm).setOnClickListener(this);
        findViewById(R.id.btn_ignoring_optimizations).setOnClickListener(this);
    }

    @Override
    protected ActivityBackgroundAlarmBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityBackgroundAlarmBinding.inflate(layoutInflater);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_alarm) {
            startService(new Intent(this, AlarmManagerCaseService.class));
        } else if (v.getId() == R.id.btn_ignoring_optimizations) {
            if (isIgnoringBatteryOptimizations()) {
                toast("当前应用:" + getPackageName() + "已经申请了忽略电池优化");
                return;
            }
            requestIgnoreBatteryOptimizations();
        }
    }

    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
