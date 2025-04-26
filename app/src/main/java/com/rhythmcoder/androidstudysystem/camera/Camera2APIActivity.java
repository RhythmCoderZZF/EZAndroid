package com.rhythmcoder.androidstudysystem.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.zzf.ezandroid.camera.EZCameraUtil;
import com.zzf.ezandroid.EZPermissionUtil;

public class Camera2APIActivity extends BaseActivity {
    private EZCameraUtil cameraUtil;
    private SurfaceView cameraPreview;
    private Handler handler = new Handler();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_camera_camera2_api);
        cameraPreview = findViewById(R.id.camera_preview);
        new EZPermissionUtil(this).requestPermission(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, granted -> {
            if (!granted) {
                finish();
            } else {
                handler.postDelayed(() -> {
                    cameraPreview.setVisibility(View.VISIBLE);
                    cameraUtil = new EZCameraUtil(this);
                    cameraUtil.startPreview(cameraPreview);
                }, 300);
            }
        });
        cameraPreview.setVisibility(View.GONE);
    }
}