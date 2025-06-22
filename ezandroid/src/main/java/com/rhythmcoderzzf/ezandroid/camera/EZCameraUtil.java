package com.rhythmcoderzzf.ezandroid.camera;

import static com.rhythmcoderzzf.ezandroid.core.ListenActivityResultFragment.holderFragmentFor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.rhythmcoderzzf.ezandroid.camera.manager.CameraSessionManager;
import com.rhythmcoderzzf.ezandroid.camera.widget.AutoFitSurfaceView;
import com.rhythmcoderzzf.ezandroid.camera.manager.FocusManager;
import com.rhythmcoderzzf.ezandroid.camera.core.Utils;
import com.rhythmcoderzzf.ezandroid.core.ListenActivityResultRequest;

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
    private Handler mCameraHandler;
    private FocusManager mFocusManager;
    private CameraSessionManager mCameraSessionManager;

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
            initModule();
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
            int width = mPreviewView.getWidth();
            int height = mPreviewView.getHeight();
            if (mPreviewView instanceof AutoFitSurfaceView) {
                Size size = new Size(width, height);
                Log.d(TAG, "surfaceCreated: size:" + size);
                Size previewSize = Utils.getPreviewOutputSize(size, characteristics, SurfaceHolder.class, null);
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
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (cameraManager == null) {
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
        try {
            //打开摄像头
            cameraManager.openCamera(mCameraId, new CameraStateCallback(), mCameraHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initModule() {
        //初始化Session管理
        mCameraSessionManager = new CameraSessionManager(mCameraDevice, characteristics, mCameraHandler, getSurfaceByPreviewView());
        mCameraSessionManager.createPreviewSession();

        //初始化焦点管理
        mFocusManager = new FocusManager(mCameraHandler, () -> {
            mCameraSessionManager.sendControlFocusModeRequest(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        });

        mFocusManager.onPreviewChanged(mPreviewView.getWidth(), mPreviewView.getHeight(), characteristics);
        mPreviewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mFocusManager.startFocus(event.getX(), event.getY());
                MeteringRectangle focusRect = mFocusManager.getFocusArea(event.getX(), event.getY(), true);
                MeteringRectangle meterRect = mFocusManager.getFocusArea(event.getX(), event.getY(), false);
                mCameraSessionManager.sendControlAfAeRequest(focusRect, meterRect);
            }
            return false;
        });
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
