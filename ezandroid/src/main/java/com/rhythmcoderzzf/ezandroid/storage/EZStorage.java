package com.rhythmcoderzzf.ezandroid.storage;

import android.content.Context;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import com.rhythmcoderzzf.ezandroid.core.AbstractBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * EZ 存储。使用方式：
 * <pre>
 *     EZStorage ezStorage = null;
 *         try {
 *             ezStorage = new EZStorage.Builder(this)
 *                     .applyFileName("temp.txt")
 *                     .applyFileType(EZStorage.TYPE_INTERNAL_DIR_CACHE)
 *                     .build();
 *             EZFileUtil.writeText(ezStorage.getOutputStream(), "hello\r\n", false);
 *             String content = EZFileUtil.readText(ezStorage.getInputStream());
 *         } catch (Exception e) {
 *             e.printStackTrace();
 *         } finally {
 *             if (ezStorage != null) {
 *                 ezStorage.release();
 *             }
 *         }
 * </pre>
 *
 * @noinspection ConstantValue
 */
public class EZStorage {
    public static final int TYPE_RAW = 1;//raw 文件类型
    public static final int TYPE_ASSETS = 2;//assets 文件类型
    /**
     * 应用内部专属空间
     */
    public static final int TYPE_INTERNAL_DIR_ROOT = 3;//data/data/[PACKAGE]/
    public static final int TYPE_INTERNAL_DIR_FILE = 4;//data/data/[PACKAGE]/files/
    public static final int TYPE_INTERNAL_DIR_CACHE = 5;//data/data/[PACKAGE]/cache/

    /**
     * 应用外部专属空间
     */
    public static final int TYPE_EXTERNAL_MEDIA = 9;//storage/emulated/0/Android/media/[package]/
    public static final int TYPE_EXTERNAL_CACHE = 10;//storage/emulated/0/Android/data/[package]/cache
    public static final int TYPE_EXTERNAL_FILE = 11;//storage/emulated/0/Android/data/[package]/files/
    /**
     * 应用外部专属空间中细分媒体类型——对应{@link # android.os.Environment}中的如下类型:
     * {@link # DIRECTORY_MUSIC}, {@link # DIRECTORY_PODCASTS},
     * {@link # DIRECTORY_RINGTONES}, {@link # DIRECTORY_ALARMS},
     * {@link # DIRECTORY_NOTIFICATIONS}, {@link # DIRECTORY_PICTURES},
     * {@link # DIRECTORY_MOVIES}, {@link # DIRECTORY_DOWNLOADS},
     * {@link # DIRECTORY_DCIM}, or {@link # DIRECTORY_DOCUMENTS}
     */
    public static final int TYPE_EXTERNAL_FILE_SUB_MUSIC = 12;//storage/emulated/0/Android/data/[package]/files/Music,Downloads...
    public static final int TYPE_EXTERNAL_FILE_SUB_MOVIES = 13;//storage/emulated/0/Android/data/[package]/files/Music,Downloads...
    public static final int TYPE_EXTERNAL_FILE_SUB_DCIM = 14;//storage/emulated/0/Android/data/[package]/files/Music,Downloads...
    public static final int TYPE_EXTERNAL_FILE_SUB_DOCUMENTS = 15;//storage/emulated/0/Android/data/[package]/files/Music,Downloads...
    public static final int TYPE_EXTERNAL_FILE_SUB_DOWNLOADS = 16;//storage/emulated/0/Android/data/[package]/files/Music,Downloads...
    public static final int TYPE_EXTERNAL_FILE_SUB_PICTURES = 17;//storage/emulated/0/Android/data/[package]/files/Music,Downloads...
    private static final int TYPE_FIRST_INTERNAL_TYPE = TYPE_INTERNAL_DIR_ROOT;
    private static final int TYPE_FIRST_EXTERNAL_TYPE = TYPE_EXTERNAL_FILE_SUB_MUSIC;
    private int mFileType;
    private File mFile;
    private Object mObjectFileName;
    private String mFileNameStr;
    private int mFileNameId;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private final Context mContext;

    private EZStorage(Context context) {
        mContext = context;
    }

    /**
     * 获取输入流
     *
     * @return 输入流
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (mInputStream != null) {
            return mInputStream;
        }
        switch (mFileType) {
            case TYPE_ASSETS:
                mInputStream = mContext.getResources().getAssets().open(mFileNameStr);
                break;
            case TYPE_RAW:
                mInputStream = mContext.getResources().openRawResource(mFileNameId);
                break;
            default:
                mInputStream = createInputStream(mFile);
                break;
        }
        return mInputStream;
    }

    /**
     * 获取输出流
     *
     * @return 输出流
     * @throws FileNotFoundException
     */
    public OutputStream getOutputStream() throws IOException {
        if (mOutputStream != null) {
            return mOutputStream;
        }
        mOutputStream = createOutputStream(mFile);
        return mOutputStream;
    }

    /**
     * 释放所有文件资源。使用完后需要调用
     */
    public void release() {
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mInputStream = null;
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mOutputStream = null;
            }
        }
    }

    /**
     * 获取File
     *
     * @return file
     */
    public File getFile() {
        return mFile;
    }

    private void adjustFileName() throws Exception {
        if (mFileType >= TYPE_FIRST_EXTERNAL_TYPE && getPrimaryExternalStorage() == null) {
            throw new IllegalStateException("无外置存储");
        }
        if (mFileType == TYPE_RAW) {
            mFileNameId = (Integer) mObjectFileName;
        } else {
            mFileNameStr = (String) mObjectFileName;
        }
        if (mFileType >= TYPE_FIRST_INTERNAL_TYPE) {
            switch (mFileType) {
                case TYPE_INTERNAL_DIR_CACHE:
                    mFile = new File(mContext.getCacheDir(), mFileNameStr);
                    break;
                case TYPE_INTERNAL_DIR_FILE:
                    mFile = new File(mContext.getFilesDir(), mFileNameStr);
                    break;
                case TYPE_INTERNAL_DIR_ROOT:
                    mFile = new File(mContext.getDataDir(), mFileNameStr);
                    break;
                case TYPE_EXTERNAL_CACHE:
                    mFile = new File(mContext.getExternalCacheDir(), mFileNameStr);
                    break;
                case TYPE_EXTERNAL_MEDIA:
                    mFile = new File(mContext.getExternalMediaDirs()[0], mFileNameStr);
                    break;
                case TYPE_EXTERNAL_FILE:
                    mFile = new File(mContext.getExternalFilesDir(null), mFileNameStr);
                    break;
                case TYPE_EXTERNAL_FILE_SUB_DCIM:
                    mFile = new File(mContext.getExternalFilesDirs(Environment.DIRECTORY_DCIM)[0], mFileNameStr);
                    break;
                case TYPE_EXTERNAL_FILE_SUB_DOCUMENTS:
                    mFile = new File(mContext.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)[0], mFileNameStr);
                    break;
                case TYPE_EXTERNAL_FILE_SUB_DOWNLOADS:
                    mFile = new File(mContext.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS)[0], mFileNameStr);
                    break;
                case TYPE_EXTERNAL_FILE_SUB_MOVIES:
                    mFile = new File(mContext.getExternalFilesDirs(Environment.DIRECTORY_MOVIES)[0], mFileNameStr);
                    break;
                case TYPE_EXTERNAL_FILE_SUB_MUSIC:
                    mFile = new File(mContext.getExternalFilesDirs(Environment.DIRECTORY_MUSIC)[0], mFileNameStr);
                    break;
                case TYPE_EXTERNAL_FILE_SUB_PICTURES:
                    mFile = new File(mContext.getExternalFilesDirs(Environment.DIRECTORY_PICTURES)[0], mFileNameStr);
                    break;
                default:
                    throw new IllegalArgumentException("非法参数");
            }
            if (!mFile.exists()) {
                mFile.createNewFile();
            }
        }
    }

    private InputStream createInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private OutputStream createOutputStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    private File getPrimaryExternalStorage() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(mContext, null);
        return externalStorageVolumes[0];
    }

    public static class Builder implements AbstractBuilder<EZStorage> {
        private final Context tmpContext;
        private int tmpFileType;
        private Object tmpFileName;

        public Builder(Context context) {
            tmpContext = context;
        }

        /**
         * 设置文件类型
         *
         * @param fileType 1.TYPE_RAW
         *                 2.TYPE_ASSETS
         *                 3.TYPE_INTERNAL_DIR_ROOT
         *                 4.TYPE_INTERNAL_DIR_FILE
         *                 5.//...
         */
        public Builder applyFileType(int fileType) {
            tmpFileType = fileType;
            return this;
        }

        /**
         * 设置文件名
         *
         * @param fileName 1.R.raw.xxx int类型
         *                 2.字符串文件名
         */
        public Builder applyFileName(Object fileName) {
            tmpFileName = fileName;
            return this;
        }


        @Override
        public EZStorage build() throws Exception {
            EZStorage ezStorage = new EZStorage(tmpContext);
            ezStorage.mFileType = tmpFileType;
            ezStorage.mObjectFileName = tmpFileName;
            ezStorage.adjustFileName();
            return ezStorage;
        }
    }
}
