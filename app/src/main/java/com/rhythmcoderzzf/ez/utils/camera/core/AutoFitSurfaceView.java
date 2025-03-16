package com.rhythmcoderzzf.ez.utils.camera.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import com.rhythmcoderzzf.ez.utils.camera.EZCameraUtil;

public class AutoFitSurfaceView extends SurfaceView {
    private static final String TAG = EZCameraUtil.TAG + AutoFitSurfaceView.class.getSimpleName();
    private float aspectRatio = 0f;


    public AutoFitSurfaceView(Context context) {
        super(context);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be
     * measured based on the ratio calculated from the parameters.
     *
     * @param width  Camera resolution horizontal size
     * @param height Camera resolution vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        aspectRatio = (float) width / (float) height;
        //如果布局尺寸 < setFixedSize尺寸（Surface尺寸），内容会被裁剪；如果布局尺寸 > Surface尺寸，内容会留黑边
        getHolder().setFixedSize(width, height);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "origin onMeasure: " + width + " x " + height);

        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height);
        } else {
            // Performs center-crop transformation of the camera frames
            int newWidth;
            int newHeight;
            float actualRatio = (width > height) ? aspectRatio : 1f / aspectRatio;
            if (width < height * actualRatio) {
                newHeight = height;
                newWidth = (int) (height * actualRatio);
            } else {
                newWidth = width;
                newHeight = (int) (width / actualRatio);
            }
            Log.d(TAG, "new Measured dimensions aspectRatio:" + aspectRatio + " set:" + newWidth + " x " + newHeight);
            setMeasuredDimension(newWidth, newHeight);
        }
    }
}
