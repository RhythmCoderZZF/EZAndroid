package com.rhythmcoderzzf.util.utils;

import static com.rhythmcoderzzf.util.utils.core.ListenActivityResultFragment.holderFragmentFor;

import android.content.Intent;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import com.rhythmcoderzzf.util.utils.core.ListenActivityResultRequest;

public class CameraUtil {
    private static String HOLDER_TAG = "camera_holder";
    private ListenActivityResultRequest mListenActivityResultRequest;

    public CameraUtil(AppCompatActivity context) {
        mListenActivityResultRequest = holderFragmentFor(HOLDER_TAG, context);
    }

    /**
     * 启动拍照
     *
     * @param callBack
     */
    public void dispatchTakePictureIntent(CameraIntentCallback callBack) {
        mListenActivityResultRequest.startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1, (requestCode, resultCode, data) -> callBack.onIntentCallback(data));
    }

    /**
     * 启动录像
     *
     * @param callBack
     */
    public void dispatchTakeVideoIntent(CameraIntentCallback callBack) {
        mListenActivityResultRequest.startActivity(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), 2, (requestCode, resultCode, data) -> callBack.onIntentCallback(data));
    }


    public interface CameraIntentCallback {
        void onIntentCallback(Intent intent);
    }

}
