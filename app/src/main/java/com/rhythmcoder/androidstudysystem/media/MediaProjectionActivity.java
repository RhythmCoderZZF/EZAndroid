package com.rhythmcoder.androidstudysystem.media;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.PixelCopy;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;


public class MediaProjectionActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 1000;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_projection);
        mSurfaceView = findViewById(R.id.surfaceView);
        findViewById(R.id.btn_record).setOnClickListener(this);

        mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            }
        }
    }

    private void startScreenCapture() {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);

        int screenDensity = metrics.densityDpi;
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        final Surface surface = mSurfaceView.getHolder().getSurface();

        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "ScreenCapture",
                100, 200, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface, null, null
        );

//        // Use a separate thread to capture the screen in a loop
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                    PixelCopy.request(surface, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
//                        @Override
//                        public void onPixelCopyFinished(int copyResult) {
//                            if (copyResult == PixelCopy.SUCCESS) {
//                                // Process the bitmap in memory
//                                processBitmap(bitmap);
//                            }
//                        }
//                    }, new Handler(Looper.getMainLooper()));
//                    try {
//                        // Capture frame every 1 second (adjust as needed)
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_record) {
            startScreenCapture();
        }
    }

    private void scanFailure() {
        toast("扫描失败");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }
}