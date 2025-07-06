package com.rhythmcoderzzf.androidstudysystem.other.softkeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class MyImeView extends View {
    public MyImeView(Context context) {
        super(context);
    }

    public MyImeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = 300;
        int measuredWidth = 1000;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
