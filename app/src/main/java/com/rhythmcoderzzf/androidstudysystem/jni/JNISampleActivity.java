package com.rhythmcoderzzf.androidstudysystem.jni;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityJniSampleBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;

public class JNISampleActivity extends BaseActivity<ActivityJniSampleBinding> {
    {
        System.loadLibrary("testcpp");
    }

    @Override
    protected ActivityJniSampleBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityJniSampleBinding.inflate(layoutInflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.tv.setText(stringFromJNI());
    }

    native String stringFromJNI();
}