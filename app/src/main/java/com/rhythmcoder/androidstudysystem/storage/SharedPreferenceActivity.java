package com.rhythmcoder.androidstudysystem.storage;

import android.os.Bundle;
import android.view.View;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.database.EZDatabase;

public class SharedPreferenceActivity extends BaseActivity implements View.OnClickListener {
    private EZDatabase.SharedPreferenceModule ezSharePreference;
    private String sharedPreferenceFileName = "MySharedPreference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_shared_preference);
        findViewById(R.id.btn_read).setOnClickListener(this);
        findViewById(R.id.btn_write).setOnClickListener(this);
        ezSharePreference = new EZDatabase(this).getSharedPreferenceModule(sharedPreferenceFileName);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_write:
                ezSharePreference.setValue("SharedPreference", "Hello SharedPreference!");
                break;
            case R.id.btn_read:
                toast(ezSharePreference.getValue("SharedPreference"));
                break;
        }
    }
}