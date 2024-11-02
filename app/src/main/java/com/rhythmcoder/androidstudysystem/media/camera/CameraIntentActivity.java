package com.rhythmcoder.androidstudysystem.media.camera;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.utils.LogUtil;

import java.util.Arrays;

public class CameraIntentActivity extends BaseActivity implements View.OnClickListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_camera_intent);
        findViewById(R.id.btn_lunch_camera).setOnClickListener(this);
        findViewById(R.id.btn_lunch_video).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_camera_id)).append(getCameraCount());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_lunch_camera) {
            dispatchTakePictureIntent();//调用相机
        } else if (id == R.id.btn_lunch_video) {
            dispatchTakeVideoIntent();//调用录像
        }
    }

    public String getCameraCount() {
        CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 获取所有摄像头 ID
            String[] cameraIdList = cameraManager.getCameraIdList();
            LogUtil.d(TAG, "摄像头id:" + Arrays.toString(cameraIdList));
            return Arrays.toString(cameraIdList);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return "null";
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            e.printStackTrace();
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        } else {
            //display error state to the user
        }
    }
}