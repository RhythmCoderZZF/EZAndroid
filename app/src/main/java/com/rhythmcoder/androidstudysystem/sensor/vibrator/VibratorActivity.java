package com.rhythmcoder.androidstudysystem.sensor.vibrator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.util.List;


public class VibratorActivity extends BaseActivity implements View.OnClickListener {
    private Vibrator mVibrator;

    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_vibrator);
        findViewById(R.id.btn_start).setOnClickListener(this);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (!mVibrator.hasVibrator()) {
            toast("设备不支持震动");
            finish();
            return;
        }
        toast("设备是否支持振幅:" + mVibrator.hasAmplitudeControl());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            //mVibrator.vibrate(VibrationEffect.createOneShot(200, 255));
        }
    }

}