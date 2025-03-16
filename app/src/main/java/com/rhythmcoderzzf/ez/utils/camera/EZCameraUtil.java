package com.rhythmcoderzzf.ez.utils.camera;

import static com.rhythmcoderzzf.ez.utils.core.ListenActivityResultFragment.holderFragmentFor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
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
                Size size = new Size(mPreviewView.getWidth(), mPreviewView.getHeight());
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
            initModule();
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
       /* mPreviewView.setOnTouchListener((v, event) -> {
            int actionMasked = MotionEventCompat.getActionMasked(event);
            int fingerX, fingerY;
            int length = (int) (mContext.getResources().getDisplayMetrics().density * 80);
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    fingerX = (int) event.getX();
                    fingerY = (int) event.getY();
                    Log.d(TAG, "onTouch: x->" + fingerX + ",y->" + fingerY);
                    *//*mIvFocus.setX(fingerX - length / 2);
                    mIvFocus.setY(fingerY - length / 2);
                    mIvFocus.setVisibility(View.VISIBLE);*//*
                    triggerFocusArea(fingerX, fingerY);
                    break;
            }

            return false;
        });*/

        mPreviewView.setOnTouchListener((v, event) -> {
            DisplayMetrics metrics = new DisplayMetrics();
            mPreviewView.getDisplay().getMetrics(metrics);
            Log.d(TAG, "init: 屏幕分辨率 metrics : " + metrics.widthPixels + " " + metrics.heightPixels);

            int screenW = metrics.widthPixels;//屏幕宽度
            int screenH = metrics.widthPixels;//屏幕宽度

            int realPreviewWidth = 4000;
            int realPreviewHeight = 3000;

            float focusX = (float) realPreviewWidth / screenW * event.getX();
            float focusY = (float) realPreviewHeight / screenH * (event.getX() + 112 * 2.54f);

            try {
                CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                Rect cropRegion = captureRequestBuilder.get(CaptureRequest.SCALER_CROP_REGION);
                Log.d(TAG, "init: cropRegion.height() " + cropRegion.height());
                float cutDx = (cropRegion.height() - 1440) / 2.0f;

                Log.d(TAG, "init: realPreviewWidth" + realPreviewWidth + " " + realPreviewHeight);

                float x1 = event.getX();
                float y1 = event.getY() + 112 * 2.54f;
                Rect rect1 = new Rect();
                rect1.left = (int) (focusX);
                rect1.top = (int) (focusY + cutDx);
                rect1.right = (int) (focusX + 50);
                rect1.bottom = (int) (focusY + cutDx + 50);
                Log.d(TAG, "init: rect1 : " + rect1.left + " " + rect1.right + " " + rect1.top + " " + rect1.bottom);


                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                rect1 = getFocusRect((int)x1,(int)event.getY(),builder);
                builder.addTarget(getSurfaceByPreviewView());
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect1, 1000)});
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect1, 1000)});
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                mCameraCaptureSession.setRepeatingRequest(builder.build(), null, mCameraHandler);

                /*builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                mCameraCaptureSession.capture(builder.build(), null, mCameraHandler);*/

                /*if (!rect1.isEmpty()) {
                    //focusView.setVisibility(View.VISIBLE);

                    Log.d(TAG, "init: x1--y1 " + x1 + " " + y1);
                    focusView.setTouchFocusRect(rect1, x1, y1);
                }*/
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return false;
        });
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
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                        // 打开闪光灯
                        //previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                        //previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
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

    private Rect getFocusRect(int x, int y,CaptureRequest.Builder builder) {
        // 获取屏幕尺寸（假设工具类已实现）
        int screenW = mPreviewView.getDisplay().getWidth();
        int screenH = mPreviewView.getDisplay().getHeight();

        // 交换宽高处理竖屏模式
        int realPreviewWidth = mPreviewView.getHeight();
        int realPreviewHeight = mPreviewView.getWidth();

        // 计算坐标映射关系
        float focusX = (realPreviewWidth / (float) screenW) * x;
        float focusY = (realPreviewHeight / (float) screenH) * y;
        Log.d(TAG, "focusX=" + focusX + ",focusY=" + focusY);

        // 获取相机传感器全尺寸区域
        Rect totalPicSize = builder.get(CaptureRequest.SCALER_CROP_REGION);
        Log.d(TAG, "camera pic area size=" + totalPicSize);

        // 计算裁剪偏移量（需处理可能的除零问题）
        int cutDx = (totalPicSize.height() - mPreviewView.getHeight()) / 2;
        Log.d(TAG, "cutDx=" + cutDx);

        // 转换dp为px（10dp的焦点框尺寸）
        int width = 20;
        int height = 20;

        // 构造最终矩形（注意坐标系转换）
        return new Rect(
                (int) focusY,                   // left
                (int) focusX + cutDx,           // top
                (int) (focusY + height),        // right
                (int) (focusX + cutDx + width)  // bottom
        );
    }
}
