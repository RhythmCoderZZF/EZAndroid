package com.rhythmcoderzzf.ez.utils.camera;

import static com.rhythmcoderzzf.ez.utils.core.ListenActivityResultFragment.holderFragmentFor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.rhythmcoderzzf.ez.utils.camera.core.AutoFitSurfaceView;
import com.rhythmcoderzzf.ez.utils.camera.core.Utils;
import com.rhythmcoderzzf.ez.utils.core.ListenActivityResultRequest;

import java.util.Arrays;

public class EZCameraUtil<T extends View> {
    public static final String TAG = EZCameraUtil.class.getSimpleName() + "_";
    private static String HOLDER_TAG = "camera_holder";
    private Context mContext;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 2;
    private ListenActivityResultRequest mListenActivityResultRequest;

    //****************************Camera2预览***************************
    private CameraManager cameraManager;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics characteristics;
    private static String mCameraId = "0";
    private T mPreviewView;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mCameraHandler;

    public EZCameraUtil(AppCompatActivity context) {
        mContext = context;
        mListenActivityResultRequest = holderFragmentFor(HOLDER_TAG, context);
    }

    /**
     * Intent启动相机APP拍照
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
     * Intent启动相机APP录像
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

    /**
     * Intent启动相机APP拍照、录像回调函数
     */
    public interface CameraIntentCallback {
        void onIntentCallback(Intent intent);
    }
    //*******************************Camera2 API********************************

    /**
     * 开启预览
     *
     * @param previewView SurfaceView、TextureView
     */
    public void startPreview(T previewView) {
        mPreviewView = previewView;
        if (previewView instanceof TextureView) {
            ((TextureView) previewView).setSurfaceTextureListener(new PreviewSurfaceTextureListener());
        } else if (previewView instanceof SurfaceView) {
            ((SurfaceView) previewView).getHolder().addCallback(new PreviewSurfaceHolderListener());
        }
    }

    private class CameraStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.d(TAG, "can't open camera onError:" + i);
        }
    }

    /**
     * 监听HolderFragment生命周期回调
     */
    private final ListenActivityResultRequest.OnLifecycleCallback onFragmentLifecycleCallback = new ListenActivityResultRequest.OnLifecycleCallback() {
        @Override
        public void onStop() {
            super.onStop();
            closeCamera();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            releaseCamera();
        }
    };

    private class PreviewSurfaceHolderListener implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            openCamera();
            if (mPreviewView instanceof AutoFitSurfaceView) {
                Size previewSize = Utils.getPreviewOutputSize(mPreviewView.getDisplay(), characteristics, SurfaceHolder.class, null);
                ((AutoFitSurfaceView) mPreviewView).setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        }
    }

    private class PreviewSurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        }
    }

    private void openCamera() {
        cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mListenActivityResultRequest.registerLifecycleListener(onFragmentLifecycleCallback);
        HandlerThread handlerThread = new HandlerThread("CameraHandler");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
        try {
            characteristics = cameraManager.getCameraCharacteristics(mCameraId);
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开摄像头
            cameraManager.openCamera(mCameraId, new CameraStateCallback(), mCameraHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        Surface surface = getSurfaceByPreviewView();
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        previewRequestBuilder.addTarget(surface);
                        /*// 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);*/
                        // 显示预览
                        mCameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "onConfigure CameraCaptureSession Failed");
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public void releaseCamera() {
        if (mCameraHandler != null) {
            mCameraHandler.getLooper().quit();
            mCameraHandler = null;
        }
    }

    private Surface getSurfaceByPreviewView() {
        if (mPreviewView instanceof TextureView) {
            SurfaceTexture surfaceTexture = ((TextureView) mPreviewView).getSurfaceTexture();
            return new Surface((surfaceTexture));
        } else if (mPreviewView instanceof SurfaceView) {
            return ((SurfaceView) mPreviewView).getHolder().getSurface();
        }
        return null;
    }
}
