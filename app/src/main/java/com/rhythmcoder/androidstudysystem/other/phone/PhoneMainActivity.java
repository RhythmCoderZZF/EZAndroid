package com.rhythmcoder.androidstudysystem.other.phone;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoderzzf.ez.utils.EZPermissionUtil;
import com.rhythmcoderzzf.ez.utils.phone.EZPhoneUtil;

public class PhoneMainActivity extends BaseActivity {
    private EZPhoneUtil mPhoneUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_phone);
        mPhoneUtil = new EZPhoneUtil(this);
        new EZPermissionUtil(this).requestPermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS}, new EZPermissionUtil.OnPermissionListener() {
            @Override
            public void onPermissionGranted(boolean granted) {
                if (granted) {
                    start();
                }
            }

        });
    }

    private void start() {
        TextView tvPhoneNumber = (TextView) findViewById(R.id.tv_phone_number);
        String phoneNumber = mPhoneUtil.getPhoneNumber().trim();
        Log.d(TAG, "phoneNumber:" + phoneNumber);
        tvPhoneNumber.append(phoneNumber == null ? "null" : phoneNumber);
    }
}