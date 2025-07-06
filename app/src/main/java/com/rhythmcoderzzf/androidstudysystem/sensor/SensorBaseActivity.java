package com.rhythmcoderzzf.androidstudysystem.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.cmd.CmdUtil;

import java.util.List;


public class SensorBaseActivity extends BaseActivity implements View.OnClickListener, SensorEventListener2 {
    public SensorManager mSensorManager;
    private List<Sensor> mSensors;

    private TextView mTv;
    private int cont = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_base);
        findViewById(R.id.btn_register_listener).setOnClickListener(this);
        mTv = findViewById(R.id.tv_result);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//        mSensors = mSensors.stream().filter(
//                new Predicate<Sensor>() {
//                    @Override
//                    public boolean test(Sensor sensor) {
//                        return sensor.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensor.getType() >= Sensor.TYPE_DEVICE_PRIVATE_BASE;
//                    }
//                }
//        ).collect(Collectors.toList());
        mTv.setText("一共" + mSensors.size() + "个连续类型传感器");
        /*
        *
        *
        * mSensorMightHaveMoreListeners = sensorMightHaveMoreListeners; //false
        mSamplingPeriodUs = samplingPeriodUs; //int samplingPeriodUs = sensor.getMinDelay();
        mMaxReportLatencyUs = maxReportLatencyUs; //0
        mIsDeviceSuspendTest = isDeviceSuspendTest; //false
        mIsIntegrationTest = isIntegrationTest; //false
        mIsAutomotiveSpecificTest = isAutomotiveSpecificTest; //false
        * */
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        runOnUiThread(() -> mTv.append("\n\n#" + (++cont) + event.sensor.toString()));
        mSensorManager.unregisterListener(this,event.sensor);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_register_listener) {
            cont = 0;
            new Thread(() -> {
                for (Sensor sensor : mSensors) {
                    CmdUtil.d(TAG, "sensor:" + sensor);
                    mSensorManager.registerListener(this, sensor, sensor.getMinDelay(), 0, null);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

        } else if (v.getId() == R.id.btn_portrait) {
        }
    }

}