package com.rhythmcoderzzf.ez.utils.camera.manager;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.rhythmcoderzzf.ez.utils.camera.EZCameraUtil;
import com.rhythmcoderzzf.ez.utils.camera.core.CoordinateTransformer;

public class FocusManager {
    private static final String TAG = EZCameraUtil.TAG + FocusManager.class.getSimpleName();
    private final Handler mHandler;
    private Rect mPreviewRect;
    private Rect mFocusRect = new Rect();

    private CoordinateTransformer mTransformer;
    private float currentX;
    private float currentY;

    private static final int HIDE_FOCUS_DELAY = 4000;

    private FocusListener focusListener;

    private Runnable mResetFocusRunnable = () -> {
        focusListener.resetTouchToFocus();
    };

    public FocusManager(Handler mCameraHandler, FocusListener listener) {
        this.mHandler = mCameraHandler;
        this.focusListener = listener;
    }

    public void onPreviewChanged(int width, int height, CameraCharacteristics c) {
        mPreviewRect = new Rect(0, 0, width, height);
        mTransformer = new CoordinateTransformer(c, rectToRectF(mPreviewRect));
    }

    public void startFocus(float x, float y) {
        currentX = x;
        currentY = y;
        mHandler.removeCallbacks(mResetFocusRunnable);
        mHandler.postDelayed(mResetFocusRunnable, HIDE_FOCUS_DELAY);
    }

    public MeteringRectangle getFocusArea(float x, float y, boolean isFocusArea) {
        currentX = x;
        currentY = y;
        if (isFocusArea) {
            return calcTapAreaForCamera2(mPreviewRect.width() / 5, 1000);
        } else {
            return calcTapAreaForCamera2(mPreviewRect.width() / 4, 1000);
        }
    }

    private MeteringRectangle calcTapAreaForCamera2(int areaSize, int weight) {
        int left = clamp((int) currentX - areaSize / 2, mPreviewRect.left, mPreviewRect.right - areaSize);
        int top = clamp((int) currentY - areaSize / 2, mPreviewRect.top, mPreviewRect.bottom - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        toFocusRect(mTransformer.toCameraSpace(rectF));
        return new MeteringRectangle(mFocusRect, weight);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private RectF rectToRectF(Rect rect) {
        return new RectF(rect);
    }

    private void toFocusRect(RectF rectF) {
        mFocusRect.left = Math.round(rectF.left);
        mFocusRect.top = Math.round(rectF.top);
        mFocusRect.right = Math.round(rectF.right);
        mFocusRect.bottom = Math.round(rectF.bottom);
    }

    public interface FocusListener {
        void resetTouchToFocus();
    }
}
