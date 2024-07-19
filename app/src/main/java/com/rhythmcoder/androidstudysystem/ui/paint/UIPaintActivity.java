package com.rhythmcoder.androidstudysystem.ui.paint;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;


import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;


public class UIPaintActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_paint);
        findViewById(R.id.btn_test).setOnClickListener(this);
        findViewById(R.id.btn_portrait).setOnClickListener(this);
        findViewById(R.id.btn_land).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_portrait) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }else if (v.getId() == R.id.btn_land) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

}