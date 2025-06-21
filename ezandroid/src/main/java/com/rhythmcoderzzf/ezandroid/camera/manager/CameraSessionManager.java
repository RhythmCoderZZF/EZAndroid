package com.zzf.ezandroid.camera.manager;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.zzf.ezandroid.camera.EZCameraUtil;

import java.util.Arrays;

public class CameraSessionManager {
    private static final String TAG = EZCameraUtil.TAG + CameraSessionManager.class.getSimpleName();
    private final CameraCharacteristics characteristics;
    private final RequestManager mRequestMgr = new RequestManager();
    private final Handler backgroundHandler;
    private CameraDevice cameraDevice;

    public CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest.Builder mCaptureBuilder;
    private Surface mSurface;
    CameraCaptureSession cameraSession;

    public CameraSessionManager(CameraDevice mCameraDevice, CameraCharacteristics characteristics, Handler backgroundHandler, Surface surface) {
        mSurface = surface;
        cameraDevice = mCameraDevice;
        this.characteristics = characteristics;
        mRequestMgr.setCharacteristics(characteristics);
        mPreviewBuilder = null;
        mCaptureBuilder = null;
        this.backgroundHandler = backgroundHandler;
    }

    public void sendControlAfAeRequest(MeteringRectangle focusRect, MeteringRectangle meteringRect) {
        CaptureRequest.Builder builder = getPreviewBuilder();
        CaptureRequest request = mRequestMgr.getTouch2FocusRequest(builder, focusRect, meteringRect);
        sendRepeatingRequest(request, null, backgroundHandler);
        // trigger af
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        sendCaptureRequest(builder.build(), null, backgroundHandler);
    }

    private CaptureRequest.Builder getPreviewBuilder() {
        if (mPreviewBuilder == null) {
            mPreviewBuilder = createBuilder(CameraDevice.TEMPLATE_PREVIEW, mSurface);
        }
        return mPreviewBuilder;
    }

    private CaptureRequest.Builder getCaptureBuilder(boolean create, Surface surface) {
        if (create) {
            return createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
        } else {
            if (mCaptureBuilder == null) {
                mCaptureBuilder = createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
            }
            return mCaptureBuilder;
        }
    }

    CaptureRequest.Builder createBuilder(int type, Surface surface) {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(type);
            builder.addTarget(surface);
            return builder;
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createPreviewSession() {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(mSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraSession = session;
                    CaptureRequest request = mRequestMgr.getPreviewRequest(getPreviewBuilder());
                    sendRepeatingRequest(request, null, backgroundHandler);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, backgroundHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void sendControlFocusModeRequest(int focusMode) {
        Log.d(TAG, "focusMode:" + focusMode);
        CaptureRequest request = mRequestMgr.getFocusModeRequest(getPreviewBuilder(), focusMode);
        sendRepeatingRequest(request, null, backgroundHandler);
    }

    void sendRepeatingRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) {
        try {
            cameraSession.setRepeatingRequest(request, callback, handler);
        } catch (CameraAccessException | IllegalStateException e) {
            Log.e(TAG, "send repeating request error:" + e.getMessage());
        }
    }

    void sendCaptureRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) {
        try {
            cameraSession.capture(request, callback, handler);
        } catch (CameraAccessException | IllegalStateException e) {
            Log.e(TAG, "send capture request error:" + e.getMessage());
        }
    }

}
