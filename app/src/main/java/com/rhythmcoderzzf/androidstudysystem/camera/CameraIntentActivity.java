package com.rhythmcoderzzf.androidstudysystem.camera;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.ezandroid.camera.EZCamera;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.EZPermission;

/**
 * <a href="https://developer.android.google.cn/media/camera/camera-intents?hl=zh-cn">相机 intent</a>
 * 如果您要使用设备的默认相机应用执行拍照或录制视频等基本相机操作，则无需与相机库集成，请改用 Intent。
 */
public class CameraIntentActivity extends BaseActivity implements View.OnClickListener {

    private EZCamera cameraUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_camera_intent);
        findViewById(R.id.btn_lunch_camera).setOnClickListener(this);
        findViewById(R.id.btn_lunch_video).setOnClickListener(this);
        new EZPermission(this).requestPermission(new String[]{Manifest.permission.CAMERA}, granted -> {

        });
        cameraUtil = new EZCamera(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_lunch_camera) {
            cameraUtil.dispatchTakePictureIntent((data) -> {

            });
        } else if (id == R.id.btn_lunch_video) {
            cameraUtil.dispatchTakeVideoIntent((data) -> {

            });
        }
    }
}