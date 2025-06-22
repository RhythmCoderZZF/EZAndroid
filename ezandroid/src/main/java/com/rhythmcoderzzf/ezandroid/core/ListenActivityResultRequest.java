package com.rhythmcoderzzf.ezandroid.core;

import android.content.Intent;

import androidx.annotation.NonNull;

public interface ListenActivityResultRequest {
    void registerLifecycleListener(OnLifecycleCallback callback);

    void startActivityForResult(Intent intent, int requestCode, OnActivityResultCallBack callBack);

    void requestPermissionForResult(String[] permissions, int requestCode, OnPermissionResultCallback callback);

    interface OnActivityResultCallBack {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    interface OnPermissionResultCallback {
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }

    public static class OnLifecycleCallback {
        public void onResume(){}
        public void onPause(){}
        public void onStop(){}
        public void onDestroy(){}
    }
}
