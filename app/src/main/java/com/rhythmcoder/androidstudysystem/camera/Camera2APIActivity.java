package com.rhythmcoder.androidstudysystem.camera;

import android.Manifest;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoderzzf.util.utils.CameraUtil;
import com.rhythmcoderzzf.util.utils.PermissionUtil;

/**
 * <a href="https://developer.android.google.cn/media/camera/camera-intents?hl=zh-cn">相机 intent</a>
 * 如果您要使用设备的默认相机应用执行拍照或录制视频等基本相机操作，则无需与相机库集成，请改用 Intent。
 */
public class Camera2APIActivity extends BaseActivity implements View.OnClickListener {
    private CameraUtil cameraUtil;
    private TextureView cameraPreview;
    private Surface previewSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_camera_camera2_api);
        new PermissionUtil(this).requestPermission(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, granted -> {
            if (!granted) {
                finish();
            }
        });

        cameraPreview = findViewById(R.id.camera_preview);
        cameraUtil = new CameraUtil(this);
        cameraUtil.setTextureView(cameraPreview);
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