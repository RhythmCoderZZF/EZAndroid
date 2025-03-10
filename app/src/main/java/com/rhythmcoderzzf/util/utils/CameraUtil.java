package com.rhythmcoderzzf.util.utils;

import static com.rhythmcoderzzf.util.utils.camera.Utils.getPreviewOutputSize;
import static com.rhythmcoderzzf.util.utils.core.ListenActivityResultFragment.holderFragmentFor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.rhythmcoderzzf.util.utils.camera.AutoFitSurfaceView;
import com.rhythmcoderzzf.util.utils.core.ListenActivityResultRequest;

import java.util.Arrays;

public class CameraUtil<T extends View> {
    private static final String TAG = CameraUtil.class.getSimpleName();
    private static String HOLDER_TAG = "camera_holder";
    private Context mContext;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 2;
    private ListenActivityResultRequest mListenActivityResultRequest;

    //****************************Camera2预览***************************
    private CameraManager cameraManager;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics characteristics;
    private static String mCameraId = "1";
    private T mTextureView;
    private Size previewSize;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mCameraHandler;

    public CameraUtil(AppCompatActivity context) {
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
     * @param textureView SurfaceView、TextureView
     */
    public void startPreview(T textureView) {
        mTextureView = textureView;
        if (textureView instanceof TextureView) {
            ((TextureView) textureView).setSurfaceTextureListener(new PreviewSurfaceTextureListener());
        } else if (textureView instanceof SurfaceView) {
            ((SurfaceView) textureView).getHolder().addCallback(new PreviewSurfaceHolderListener());
        }
    }

    private class CameraStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            takePreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    }

    /**
     * 监听HolderFragment生命周期回调
     */
    private ListenActivityResultRequest.OnLifecycleCallback onFragmentLifecycleCallback = new ListenActivityResultRequest.OnLifecycleCallback() {
        @Override
        public void onResume() {
            super.onResume();
            if (mCameraDevice != null) {
                openCamera();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            try {
                if (mCameraDevice != null) mCameraDevice.close();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
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
            initCamera();
            if (mTextureView instanceof AutoFitSurfaceView) {
                Size previewSize = getPreviewOutputSize(mTextureView.getDisplay(), characteristics, SurfaceHolder.class, ImageFormat.JPEG);
                ((AutoFitSurfaceView) mTextureView).setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            }
            openCamera();
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
            initCamera();
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

    private void initCamera() {
        cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mListenActivityResultRequest.registerLifecycleListener(onFragmentLifecycleCallback);
        HandlerThread handlerThread = new HandlerThread("CameraHandler");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
        try {
            characteristics = cameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * TextureView的surface初始化完毕，开始openCamera流程
     */
    private void openCamera() {
        try {
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
    private void takePreview() {
        Surface surface = getSurfaceByPreviewView();
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        previewRequestBuilder.addTarget(surface);
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        //previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        mCameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    cameraCaptureSession.close();
                    mCameraCaptureSession = null;
                    if (mCameraDevice != null) {
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        /*if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }*/
        if (mCameraHandler != null) {
            mCameraHandler.getLooper().quit();
            mCameraHandler = null;
        }
    }

    private Surface getSurfaceByPreviewView() {
        if (mTextureView instanceof TextureView) {
            if (previewSize != null) {
                SurfaceTexture surfaceTexture = ((TextureView) mTextureView).getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                return new Surface((surfaceTexture));
            }
        } else if (mTextureView instanceof SurfaceView) {
            return ((SurfaceView) mTextureView).getHolder().getSurface();
        }
        return null;
    }
}
