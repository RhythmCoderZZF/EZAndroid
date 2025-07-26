package com.rhythmcoderzzf.androidstudysystem.ui.virtualdisplay;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityUiVirtualDisplayBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.utils.EZLogUtil;

import java.util.Arrays;


public class UIVirtualDisplayActivity extends BaseActivity {
    private SecondaryScreen mSecondaryScreen;
    private MyPresentation mPresentation;

    @Override
    protected ActivityUiVirtualDisplayBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityUiVirtualDisplayBinding.inflate(layoutInflater);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayManager mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = mDisplayManager.getDisplays();
        EZLogUtil.d(TAG, "displays:" + Arrays.toString(displays));
        mSecondaryScreen = new SecondaryScreen(this);
        mSecondaryScreen.createVirtualDisplay();
        mPresentation = new MyPresentation(this, mDisplayManager.getDisplays()[1]);
        mPresentation.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresentation.dismiss();
        mSecondaryScreen.release();
    }
}