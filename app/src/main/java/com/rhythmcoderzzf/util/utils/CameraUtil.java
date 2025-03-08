package com.rhythmcoderzzf.util.utils;

import static com.rhythmcoderzzf.util.utils.core.ListenActivityResultFragment.holderFragmentFor;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import com.rhythmcoderzzf.util.utils.core.ListenActivityResultRequest;

public class CameraUtil {
    private static String HOLDER_TAG = "camera_holder";
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 2;
    private ListenActivityResultRequest mListenActivityResultRequest;
    private Context mContext;

    public CameraUtil(AppCompatActivity context) {
        mContext = context;
        mListenActivityResultRequest = holderFragmentFor(HOLDER_TAG, context);
    }

    /**
     * 启动拍照
     *
     * @param callBack
     */
    public void dispatchTakePictureIntent(CameraIntentCallback callBack) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mListenActivityResultRequest.startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE, (requestCode, resultCode, data) -> callBack.onIntentCallback(data));
        } else {
            //display error state to the user
        }
    }

    /**
     * 启动录像
     *
     * @param callBack
     */
    public void dispatchTakeVideoIntent(CameraIntentCallback callBack) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mListenActivityResultRequest.startActivityForResult(intent, REQUEST_CODE_VIDEO_CAPTURE, (requestCode, resultCode, data) -> callBack.onIntentCallback(data));
        } else {
            //display error state to the user
        }
    }

    public interface CameraIntentCallback {
        void onIntentCallback(Intent intent);
    }

}
