package com.rhythmcoderzzf.androidstudysystem.other.phone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.EZPermission;
import com.rhythmcoderzzf.ezandroid.telephone.EZTelephone;

public class PhoneMainActivity extends BaseActivity {
    private EZTelephone mPhoneUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_phone);
        mPhoneUtil = new EZTelephone(this);
        new EZPermission(this).requestPermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS}, new EZPermission.OnPermissionListener() {
            @Override
            public void onPermissionGranted(boolean granted) {
                if (granted) {
                    start();
                }
            }

        });
    }
    @SuppressLint("MissingPermission")
    private void start() {
        TextView tvPhoneNumber = (TextView) findViewById(R.id.tv_phone_number);
        String phoneNumber = mPhoneUtil.getPhoneNumber().trim();
        Log.d(TAG, "phoneNumber:" + phoneNumber);
        tvPhoneNumber.append(phoneNumber);
    }
}