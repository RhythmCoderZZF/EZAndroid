package com.rhythmcoder.androidstudysystem.background.service;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;

public class BackgroundServiceActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_service);
    }
}
