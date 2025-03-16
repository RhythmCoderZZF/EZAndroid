package com.rhythmcoderzzf.ez.utils.camera.core;

import android.graphics.Point;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;
import android.view.Display;

import com.rhythmcoderzzf.ez.utils.camera.EZCameraUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.ToIntFunction;

public class Utils {
    public static final String TAG = EZCameraUtil.TAG + Utils.class.getSimpleName();

    // Standard High Definition size for pictures and video (1080P)
    public static final SmartSize SIZE_1080P = new SmartSize(1920, 1080);

    /**
     * Returns a [SmartSize] object for the given [Display]
     *
     * @param display The display for which we want to calculate the size.
     * @return SmartSize object containing the size of the display.
     */
    public static SmartSize getDisplaySmartSize(Display display) {
        Point outPoint = new Point();
        display.getRealSize(outPoint);
        return new SmartSize(outPoint.x, outPoint.y);
    }

    /**
     * 根据屏幕尺寸和1080P限制，从相机支持的输出尺寸中选择最大的合适预览尺寸。
     *
     * @param display         The display where the preview size will be shown.
     * @param characteristics CameraCharacteristics object to get the camera configuration.
     * @param targetClass     The class of the target (e.g., SurfaceTexture.class).
     * @param format          The image format (optional).
     * @return The largest available preview size.
     */
    public static <T> Size getPreviewOutputSize(Display display, CameraCharacteristics characteristics, Class<T> targetClass, Integer format) {
        // Find which is smaller: screen or 1080p
        SmartSize screenSize = getDisplaySmartSize(display);
        boolean hdScreen = screenSize.getLong() >= SIZE_1080P.getLong() || screenSize.getShort() >= SIZE_1080P.getShort();
        SmartSize maxSize = hdScreen ? SIZE_1080P : screenSize;
        // Get the camera configuration
        StreamConfigurationMap config = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        // Get all available sizes based on the format or target class
        Size[] allSizes;
        if (format == null) {
            allSizes = config.getOutputSizes(targetClass);
        } else {
            allSizes = config.getOutputSizes(format);
        }
        Log.d(TAG, "get preview Output Size,allSizes:" + Arrays.toString(allSizes));
        // Sort the sizes by area from largest to smallest
        SmartSize[] validSizes = Arrays.stream(allSizes).map(size -> new SmartSize(size.getHeight(), size.getWidth())).sorted(Comparator.comparingInt(size -> size.getSize().getHeight() * size.getSize().getWidth())).toArray(SmartSize[]::new);
        // Find the largest size that is smaller or equal to maxSize
        for (SmartSize validSize : validSizes) {
            if (validSize.getLong() <= maxSize.getLong() && validSize.getShort() <= maxSize.getShort()) {
                Log.d(TAG, "Find the largest size that is smaller or equal to maxSize,size:" + validSize.getSize());
                return validSize.getSize();
            }
        }
        // Return null if no suitable size is found
        return null;
    }

    /**
     * Helper class to store the dimensions of a Size object, providing the longest and shortest side.
     */
    public static class SmartSize {
        private Size size;
        private int longSide;
        private int shortSide;

        public SmartSize(int width, int height) {
            this.size = new Size(width, height);
            this.longSide = Math.max(width, height);
            this.shortSide = Math.min(width, height);
        }

        public int getLong() {
            return longSide;
        }

        public int getShort() {
            return shortSide;
        }

        public Size getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "SmartSize(" + longSide + "x" + shortSide + ")";
        }
    }

    /**
     * 要求相机的 Hardware Level 必须是 FULL 及以上，才能支持Camera2 API
     *
     * @param characteristics 相机信息的提供者
     * @param requiredLevel   Hardware Level
     * @return
     */
    private static boolean isHardwareLevelSupported(CameraCharacteristics characteristics, int requiredLevel) {
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
    }

}
