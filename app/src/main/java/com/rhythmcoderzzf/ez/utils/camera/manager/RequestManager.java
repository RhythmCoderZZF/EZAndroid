package com.rhythmcoderzzf.ez.utils.camera.manager;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Log;

import com.rhythmcoderzzf.ez.utils.camera.EZCameraUtil;

public class RequestManager {
    private static final String TAG = EZCameraUtil.TAG + RequestManager.class.getSimpleName();
    private MeteringRectangle[] mFocusArea;
    private MeteringRectangle[] mMeteringArea;
    private MeteringRectangle[] mResetRect = new MeteringRectangle[]{new MeteringRectangle(0, 0, 0, 0, 0)};
    private CameraCharacteristics mCharacteristics;

    public void setCharacteristics(CameraCharacteristics characteristics) {
        mCharacteristics = characteristics;
    }

    public CaptureRequest getPreviewRequest(CaptureRequest.Builder builder) {
        int afMode = getValidAFMode(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        int antiBMode = getValidAntiBandingMode(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antiBMode);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    public CaptureRequest getFocusModeRequest(CaptureRequest.Builder builder, int focusMode) {
        int afMode = getValidAFMode(focusMode);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.CONTROL_AF_REGIONS, mResetRect);
        builder.set(CaptureRequest.CONTROL_AE_REGIONS, mResetRect);
        // cancel af trigger
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    public CaptureRequest getTouch2FocusRequest(CaptureRequest.Builder builder, MeteringRectangle focus, MeteringRectangle metering) {
        int afMode = getValidAFMode(CaptureRequest.CONTROL_AF_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        if (mFocusArea == null) {
            mFocusArea = new MeteringRectangle[]{focus};
        } else {
            mFocusArea[0] = focus;
        }
        if (mMeteringArea == null) {
            mMeteringArea = new MeteringRectangle[]{metering};
        } else {
            mMeteringArea[0] = metering;
        }
        if (isMeteringSupport(true)) {
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, mFocusArea);
        }
        if (isMeteringSupport(false)) {
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, mMeteringArea);
        }
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    /* ------------------------- private function------------------------- */
    private int getValidAFMode(int targetMode) {
        int[] allAFMode = mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        for (int mode : allAFMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        Log.d(TAG, "not support af mode:" + targetMode + " use mode:" + allAFMode[0]);
        return allAFMode[0];
    }

    private boolean isMeteringSupport(boolean focusArea) {
        int regionNum;
        if (focusArea) {
            regionNum = mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        } else {
            regionNum = mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
        }
        return regionNum > 0;
    }

    private int getValidAntiBandingMode(int targetMode) {
        int[] allABMode = mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        for (int mode : allABMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        Log.i(TAG, "not support anti banding mode:" + targetMode + " use mode:" + allABMode[0]);
        return allABMode[0];
    }


}
