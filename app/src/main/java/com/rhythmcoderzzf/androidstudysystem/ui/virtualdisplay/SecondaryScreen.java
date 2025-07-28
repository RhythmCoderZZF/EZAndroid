package com.rhythmcoderzzf.androidstudysystem.ui.virtualdisplay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.widget.ImageView;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.ezandroid.utils.EZLogUtil;

import java.nio.ByteBuffer;

public class SecondaryScreen {
    private static final String TAG = "SecondaryScreen";
    public static final String DISPLAY_NAME = "ST77916TestDisplay";
    private final DisplayManager mDisplayManager;
    private final Activity mContext;
    private final ImageView imageView;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;

    private final int VIRTUAL_DISPLAY_WIDTH = 200; // 虚拟显示器宽度
    private final int VIRTUAL_DISPLAY_HEIGHT = 400; // 虚拟显示器高度
    private static final int VIRTUAL_DISPLAY_DPI = 160;

    private final HandlerThread handlerThread;
    private final Handler mBackgroundHandler;

    public SecondaryScreen(Context context) {
        this.mContext = (Activity) context;
        handlerThread = new HandlerThread("HanderThread");
        handlerThread.start();
        mBackgroundHandler = new Handler(handlerThread.getLooper());
        mDisplayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        imageView = mContext.findViewById(R.id.imageView);
    }

    public void createVirtualDisplay() {
        if (mVirtualDisplay != null) {
            return;
        }
        mImageReader = ImageReader.newInstance(VIRTUAL_DISPLAY_WIDTH, VIRTUAL_DISPLAY_HEIGHT, PixelFormat.RGBA_8888, 2);
        Canvas canvas = mImageReader.getSurface().lockHardwareCanvas();
        Matrix matrix = new Matrix();
        matrix.postRotate(90, canvas.getWidth() / 2f, canvas.getHeight() / 2f);
        canvas.setMatrix(matrix);
        mImageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                Bitmap resultBitmap = imageToBitmap(image);
                if (resultBitmap != null) {
                    mContext.runOnUiThread(() -> imageView.setImageBitmap(resultBitmap));
                }
            }
        }, mBackgroundHandler);
        Surface mSurface = mImageReader.getSurface();
        mVirtualDisplay = mDisplayManager.createVirtualDisplay(DISPLAY_NAME, VIRTUAL_DISPLAY_WIDTH, VIRTUAL_DISPLAY_HEIGHT, VIRTUAL_DISPLAY_DPI, mSurface, DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY, null, mBackgroundHandler);
    }

    public void release() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        handlerThread.quit();
    }


    public Bitmap imageToBitmap(Image image) {
        try {
            Image.Plane[] planes = image.getPlanes();
            if (planes != null) {
                if (planes.length != 0) {
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int width = image.getWidth();
                    int height = image.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(((rowStride - (pixelStride * width)) / pixelStride) + width, height, Bitmap.Config.ARGB_8888);
                    buffer.rewind();
                    bitmap.copyPixelsFromBuffer(buffer);
                    if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
                        return bitmap;
                    }
                    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                    bitmap.recycle();
                    return croppedBitmap;
                }
            }
            EZLogUtil.e(TAG, "Image planes are null or empty.");
            return null;
        } catch (Exception e) {
            EZLogUtil.e(TAG, "将 Image 转换为 Bitmap 时出错: " + e.getMessage(), e);
            return null;
        }
    }
}
