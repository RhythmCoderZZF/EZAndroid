package com.rhythmcoder.androidstudysystem.other;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.misc.EZShellUtil;

public class ADBShellActivity extends BaseActivity {
    private String mShellWords = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_adb_shell);
        EditText editText = ((EditText) findViewById(R.id.edt_shell));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mShellWords = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            toast(mShellWords);
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String string = EZShellUtil.exeCmd(mShellWords);
                TextView textView = findViewById(R.id.tv_shell);
                textView.setVisibility(View.VISIBLE);
                textView.setText(string);
            }
            return false;
        });
    }

}