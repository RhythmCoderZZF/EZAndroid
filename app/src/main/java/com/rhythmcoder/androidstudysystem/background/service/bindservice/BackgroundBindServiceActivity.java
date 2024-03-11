package com.rhythmcoder.androidstudysystem.background.service.bindservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.Nullable;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.cmd.CmdUtil;

public class BackgroundBindServiceActivity extends BaseActivity implements View.OnClickListener {
    private Intent mIntent;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CmdUtil.i(TAG, "onServiceConnected<<");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CmdUtil.i(TAG, "onServiceDisconnected<<");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_bind_service);
        findViewById(R.id.btn_bind).setOnClickListener(this);
        findViewById(R.id.btn_call).setOnClickListener(this);
        findViewById(R.id.btn_unbind).setOnClickListener(this);
        mIntent = new Intent(this, BindService.class);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_bind) {
            bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
        } else if (v.getId() == R.id.btn_call) {
            stopService(mIntent);
        } else if (v.getId() == R.id.btn_unbind) {
            unbindService(mConnection);
        }
    }
}
