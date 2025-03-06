package com.rhythmcoderzzf.util.utils.core;

import android.content.Intent;

import androidx.annotation.NonNull;

public interface ListenActivityResultRequest {
    void startActivity(Intent intent, int requestCode, OnActivityResultCallBack callBack);

    void requestPermission(String[] permissions, int requestCode, OnPermissionResultCallback callback);

    interface OnActivityResultCallBack {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    interface OnPermissionResultCallback {
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }
}
