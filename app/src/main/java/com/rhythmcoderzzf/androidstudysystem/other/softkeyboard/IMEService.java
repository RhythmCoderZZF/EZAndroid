package com.rhythmcoderzzf.androidstudysystem.other.softkeyboard;

import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.rhythmcoderzzf.androidstudysystem.R;

public class IMEService extends InputMethodService {
    View view;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("zzf", "hhh" + newConfig.toString());
    }

    @Override
    public View onCreateInputView() {
        view  = getLayoutInflater().inflate(R.layout.view_ime_keyboard, null);
        return view;
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        super.onStartInputView(editorInfo, restarting);
        view.requestLayout();
    }
}
