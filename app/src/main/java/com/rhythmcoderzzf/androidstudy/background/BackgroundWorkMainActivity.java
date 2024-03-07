package com.rhythmcoderzzf.androidstudy.background;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rhythmcoderzzf.androidstudy.R;
import com.rhythmcoderzzf.baselib.BaseActivity;

public class BackgroundWorkMainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_work);
        findViewById(R.id.btn_alarm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_alarm) {
            startService(new Intent(this, AlarmManagerCaseService.class));
        }
    }

}