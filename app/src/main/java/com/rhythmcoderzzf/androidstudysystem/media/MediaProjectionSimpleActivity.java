package com.rhythmcoderzzf.androidstudysystem.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;

public class MediaProjectionSimpleActivity extends BaseActivity implements View.OnClickListener {
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private SurfaceView mSurfaceView;

    //【#1】通过获取一个令牌来启动媒体投影会话，该令牌会授权您的应用捕获设备显示屏或应用窗口的内容。该令牌由 MediaProjection 类的一个实例表示
    ActivityResultLauncher<Intent> startMediaProjection = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            try {
                mediaProjection = mediaProjectionManager.getMediaProjection(result.getResultCode(), result.getData());
                DisplayMetrics metrics = new DisplayMetrics();
                Display display = getWindowManager().getDefaultDisplay();
                display.getMetrics(metrics);
                int screenDensity = metrics.densityDpi;
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                mSurfaceView.setZOrderOnTop(true);
                mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                final Surface surface = mSurfaceView.getHolder().getSurface();

                //【#2】媒体投影的核心是虚拟屏幕，您可以通过对 MediaProjection 实例调用 createVirtualDisplay() 来创建虚拟屏幕
                mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", width, height, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_projection);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mSurfaceView = findViewById(R.id.surfaceView);
        findViewById(R.id.btn_record).setOnClickListener(this);
    }

    private void startScreenCapture() {
        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent());
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_record) {
            startScreenCapture();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }
}