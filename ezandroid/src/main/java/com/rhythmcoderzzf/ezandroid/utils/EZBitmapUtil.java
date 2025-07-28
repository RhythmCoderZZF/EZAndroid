package com.rhythmcoderzzf.ezandroid.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.LruCache;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EZBitmapUtil {

    // 内存缓存（使用LRU算法）
    private static final LruCache<String, Bitmap> bitmapCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 1024 / 4)) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    // ------------------------- 基础转换方法 -------------------------
    /**
     * Drawable 转 Bitmap
     * @param drawable 源Drawable
     * @return 转换后的Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth(),
            drawable.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Bitmap 转字节数组
     * @param bitmap 源Bitmap
     * @param format 压缩格式（JPEG/PNG/WEBP）
     * @return 字节数组
     */
    public static byte[] bitmapToBytes(Bitmap bitmap, Bitmap.CompressFormat format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, stream);
        return stream.toByteArray();
    }

    // ------------------------- 图片操作 -------------------------
    /**
     * 缩放图片
     * @param src 源Bitmap
     * @param newWidth 目标宽度
     * @param newHeight 目标高度
     * @return 缩放后的Bitmap
     */
    public static Bitmap scaleBitmap(Bitmap src, int newWidth, int newHeight) {
        Matrix matrix = new Matrix();
        matrix.postScale(
            (float) newWidth / src.getWidth(),
            (float) newHeight / src.getHeight()
        );
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * 旋转图片
     * @param src 源Bitmap
     * @param degrees 旋转角度（顺时针）
     * @return 旋转后的Bitmap
     */
    public static Bitmap rotateBitmap(Bitmap src, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * 裁剪图片
     * @param src 源Bitmap
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param width 裁剪宽度
     * @param height 裁剪高度
     * @return 裁剪后的Bitmap
     */
    public static Bitmap cropBitmap(Bitmap src, int x, int y, int width, int height) {
        return Bitmap.createBitmap(src, x, y, width, height);
    }

    // ------------------------- 图片压缩 -------------------------
    /**
     * 质量压缩（降低文件大小）
     * @param src 源Bitmap
     * @param maxSize 最大允许大小（KB）
     * @return 压缩后的Bitmap
     */
    public static Bitmap compressByQuality(Bitmap src, int maxSize) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int quality = 100;
        src.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        while (stream.toByteArray().length / 1024 > maxSize && quality > 10) {
            stream.reset();
            quality -= 15;
            src.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        }
        return BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
    }

    /**
     * 尺寸压缩（降低分辨率）
     * @param src 源Bitmap
     * @param sampleSize 采样率（2表示宽高各缩小一半）
     * @return 压缩后的Bitmap
     */
    public static Bitmap compressBySize(Bitmap src, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size(), options);
    }

    // ------------------------- 图片保存 -------------------------
    /**
     * 保存Bitmap到本地文件
     * @param bitmap 源Bitmap
     * @param filePath 文件路径
     * @param format 压缩格式
     * @return 是否保存成功
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, String filePath, Bitmap.CompressFormat format) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            bitmap.compress(format, 100, fos);
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 保存图片到系统相册
     * @param context 上下文
     * @param bitmap 源Bitmap
     * @param folderName 相册目录名（如"MyApp"）
     */
    public static void saveToGallery(Context context, Bitmap bitmap, String folderName) {
        File dir = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!dir.exists()) dir.mkdirs();
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);
        saveBitmapToFile(bitmap, file.getAbsolutePath(), Bitmap.CompressFormat.JPEG);
        // 通知系统扫描新文件
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    // ------------------------- 内存优化 -------------------------
    /**
     * 添加Bitmap到缓存
     * @param key 缓存键
     * @param bitmap 源Bitmap
     */
    public static void addToCache(String key, Bitmap bitmap) {
        bitmapCache.put(key, bitmap);
    }

    /**
     * 从缓存获取Bitmap
     * @param key 缓存键
     * @return 缓存的Bitmap（可能为null）
     */
    public static Bitmap getFromCache(String key) {
        return bitmapCache.get(key);
    }

    /**
     * 释放Bitmap资源（避免OOM）
     * @param bitmap 待释放的Bitmap
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}