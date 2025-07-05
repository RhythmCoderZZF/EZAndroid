package com.rhythmcoder.androidstudysystem.storage;

import android.os.Bundle;
import android.view.View;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.storage.EZStorage;

public class StorageSharedPreferenceActivity extends BaseActivity implements View.OnClickListener {
    private EZStorage.SharedPreferenceModule ezSharePreference;
    private String sharedPreferenceFileName = "MySharedPreference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_shared_preference);
        findViewById(R.id.btn_read).setOnClickListener(this);
        findViewById(R.id.btn_write).setOnClickListener(this);
        ezSharePreference = EZStorage.getInstance(this).
                loadSharedPreference(sharedPreferenceFileName, true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_write:
                ezSharePreference.putString("hello", "Jack");
                toast("写入Key:hello,value:Jack");
                break;
            case R.id.btn_read:
                String value = ezSharePreference.getString("hello", "");
                toast("读取Key:hello,value" + value);
                break;
        }
    }
}