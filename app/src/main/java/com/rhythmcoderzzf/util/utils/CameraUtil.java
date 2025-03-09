package com.rhythmcoderzzf.util.utils;

import static com.rhythmcoderzzf.util.utils.core.ListenActivityResultFragment.holderFragmentFor;

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
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.rhythmcoderzzf.util.utils.core.ListenActivityResultRequest;

import java.util.Arrays;

public class CameraUtil {
    private static final String TAG = CameraUtil.class.getSimpleName();
    private static String HOLDER_TAG = "camera_holder";
    private Context mContext;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 2;
    private ListenActivityResultRequest mListenActivityResultRequest;

    //****************************Camera2预览***************************
    private CameraManager cameraManager;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private CameraCaptureSession mCameraCaptureSession;
    private static String mCameraID;
    private Handler mCameraHandler;

    public CameraUtil(AppCompatActivity context) {
        mContext = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mListenActivityResultRequest = holderFragmentFor(HOLDER_TAG, context);
        mListenActivityResultRequest.registerLifecycleListener(onFragmentLifecycleCallback);
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
    public void setTextureView(TextureView textureView) {
        mTextureView = textureView;
        textureView.setSurfaceTextureListener(new PreviewSurfaceTextureListener());
    }

    public Size getOptimalSize(CameraCharacteristics characteristics, int maxWidth, int maxHeight) {
        return getOptimalSize(characteristics, SurfaceTexture.class, maxWidth, maxHeight);
    }

    /**
     * 判断相机的 Hardware Level 是否大于等于指定的 Level。
     */
   /* private static void getFrontAndBackCameras(CameraManager cameraManager) {
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            Log.d(TAG, "get cameraIdList:" + Arrays.toString(cameraIdList));
            for (String cameraId : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                // Check if the camera supports the required hardware level
                if (isHardwareLevelSupported(cameraCharacteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)) {
                    // Check if the camera is front-facing or back-facing
                    Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    mCameraID = cameraId;
                    mCameraCharacteristics = cameraCharacteristics;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * 要求相机的 Hardware Level 必须是 FULL 及以上，才能支持Camera2 API
     *
     * @param characteristics 相机信息的提供者
     * @param requiredLevel   Hardware Level
     * @return
     */
    /*private static boolean isHardwareLevelSupported(CameraCharacteristics characteristics, int requiredLevel) {
        int[] sortedLevels = new int[]{CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3};

        Integer deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (requiredLevel == deviceLevel) {
            return true;
        }

        for (int sortedLevel : sortedLevels) {
            if (requiredLevel == sortedLevel) {
                return true;
            } else if (deviceLevel == sortedLevel) {
                return false;
            }
        }
        return false;
    }*/

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
        }

        @Override
        public void onPause() {
            super.onPause();
            //if (onFragmentLifecycleCallback != null) closeCamera();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            cancelCamera();
        }
    };

    private class PreviewSurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: ");
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureUpdated: ");
        }
    }

    /**
     * 根据我们的实际要求，获取相机支持的最适合的预览尺寸
     *
     * @param cameraCharacteristics
     * @param clazz                 ImageReader：常用来拍照或接收 YUV 数据。
     *                              MediaRecorder：常用来录制视频。
     *                              MediaCodec：常用来录制视频。
     *                              SurfaceHolder：常用来显示预览画面。
     *                              SurfaceTexture：常用来显示预览画面。
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    private Size getOptimalSize(CameraCharacteristics cameraCharacteristics, Class<?> clazz, int maxWidth, int maxHeight) {
        float aspectRatio = (float) maxWidth / maxHeight;
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (streamConfigurationMap != null) {
            Size[] supportedSizes = streamConfigurationMap.getOutputSizes(clazz);
            if (supportedSizes != null) {
                for (Size size : supportedSizes) {
                    if ((float) size.getWidth() / size.getHeight() == aspectRatio && size.getHeight() <= maxHeight && size.getWidth() <= maxWidth) {
                        return size;
                    }
                }
            }
        }
        return null;
    }

    /**
     * TextureView的surface初始化完毕，开始openCamera流程
     */
    public void openCamera() {
        HandlerThread handlerThread = new HandlerThread("CameraHandler");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
        mCameraID = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
        try {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开摄像头
            cameraManager.openCamera(mCameraID, new CameraStateCallback(), mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(new Surface(mTextureView.getSurfaceTexture()));
            // 自动对焦
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(new Surface(mTextureView.getSurfaceTexture())), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 创建预览需要的CaptureRequest.Builder
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

    public void cancelCamera() {
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
        stopBackgroundThread(); // 对应 openCamera() 方法中的 startBackgroundThread()
    }

    private void stopBackgroundThread() {
        if (mCameraHandler != null) {
            mCameraHandler.getLooper().quit();
            mCameraHandler = null;
            mCameraHandler = null;
        }
    }
}
