package com.rhythmcoderzzf.androidstudysystem.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.camera.EZCamera;
import com.rhythmcoderzzf.ezandroid.permission.EZPermission;

public class Camera2APIActivity extends BaseActivity {
    private EZCamera cameraUtil;
    private SurfaceView cameraPreview;
    private Handler handler = new Handler();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_camera_camera2_api);
        cameraPreview = findViewById(R.id.camera_preview);
        new EZPermission.Builder(this).applyRequestPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).build().requestPermission((deniedPermissions) -> {
            if (deniedPermissions.isEmpty()) {
                toast("请打开权限");
                finish();
            } else {
                handler.postDelayed(() -> {
                    cameraPreview.setVisibility(View.VISIBLE);
                    cameraUtil = new EZCamera(this);
                    cameraUtil.startPreview(cameraPreview);
                }, 300);
            }
        });
        cameraPreview.setVisibility(View.GONE);
    }
}