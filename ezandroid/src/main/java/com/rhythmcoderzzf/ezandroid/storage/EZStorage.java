package com.rhythmcoderzzf.ezandroid.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Easy Storage API.分为如下几个子模块：
 * SharedPreferenceModule、AppSpecificModule
 */
public class EZStorage {
    private final Context mContext;

    private EZStorage(Context context) {
        mContext = context;
    }

    public static EZStorage getInstance(Context context) {
        return new EZStorage(context);
    }

    public SharedPreferenceModule loadSharedPreference(String preferenceFileName, boolean applyMode) {
        return new SharedPreferenceModule(preferenceFileName, applyMode);
    }

    public AppSpecificModule loadAppSpecific() {
        return new AppSpecificModule();
    }

    /**
     * Shared Preference模块
     */
    public class SharedPreferenceModule {
        private final String mPreferenceFileName;
        private boolean mApplyMode;
        private final SharedPreferences mSharedPreferences;
        private final SharedPreferences.Editor mEditor;

        public SharedPreferenceModule(String preferenceFileName, boolean applyMode) {
            this.mPreferenceFileName = preferenceFileName;
            this.mApplyMode = applyMode;
            mSharedPreferences = mContext.getSharedPreferences(mPreferenceFileName, Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
        }

        public void putString(String key, String value) {
            mEditor.putString(key, value);
            if (mApplyMode) mEditor.apply();
            else mEditor.commit();
        }

        public void putInt(String key, int value) {
            mEditor.putInt(key, value);
            if (mApplyMode) mEditor.apply();
            else mEditor.commit();
        }

        public void putBoolean(String key, boolean value) {
            mEditor.putBoolean(key, value);
            if (mApplyMode) mEditor.apply();
            else mEditor.commit();
        }

        public String getString(String key, String defValue) {
            return mSharedPreferences.getString(key, defValue);
        }

        public boolean getBoolean(String key, boolean defValue) {
            return mSharedPreferences.getBoolean(key, defValue);
        }

        public int getInt(String key, int defValue) {
            return mSharedPreferences.getInt(key, defValue);
        }

    }

    /**
     * 应用专属存储空间
     * 1. /data/data/[PACKAGE]/*
     * 2. Raw Resource、Assets
     */
    public class AppSpecificModule {
        /* 应用专属空间——私有 */
        public static final int TYPE_RAW = 1;//res/raw/
        public static final int TYPE_ASSETS = 2;//assets/
        public static final int TYPE_DIR_ROOT = 3;//data/data/[PACKAGE]/
        public static final int TYPE_DIR_FILE = 4;//data/data/[PACKAGE]/files/
        public static final int TYPE_DIR_CACHE = 5;//data/data/[PACKAGE]/cache/
        private static final int TYPE_LAST_INTERNAL_TYPE = TYPE_DIR_CACHE;//data/data/[PACKAGE]/cache/
        /* 应用专属空间——外部 */
        public static final int TYPE_EXTERNAL_MEDIA = 9;//storage/emulated/0/Android/media/[package]/
        public static final int TYPE_EXTERNAL_CACHE = 10;//storage/emulated/0/Android/data/[package]/cache
        public static final int TYPE_EXTERNAL_FILE = 11;//storage/emulated/0/Android/data/[package]/files/
        /**
         * 启用subType，subType使用 {@link # android.os.Environment}:
         * {@link # DIRECTORY_MUSIC}, {@link # DIRECTORY_PODCASTS},
         * {@link # DIRECTORY_RINGTONES}, {@link # DIRECTORY_ALARMS},
         * {@link # DIRECTORY_NOTIFICATIONS}, {@link # DIRECTORY_PICTURES},
         * {@link # DIRECTORY_MOVIES}, {@link # DIRECTORY_DOWNLOADS},
         * {@link # DIRECTORY_DCIM}, or {@link # DIRECTORY_DOCUMENTS}
         */
        public static final int TYPE_EXTERNAL_FILE_SUB = 12;//storage/emulated/0/Android/data/[package]/files/Music,Downloads...
        private int mType = 0;
        private File mFile;
        private String mFileNameStr;
        private int mFileNameId;
        private InputStream mInputStream;
        private OutputStream mOutputStream;

        /**
         * 应用专属文件
         *
         * @param fileName 文件名，file.txt
         * @param type     文件类型
         * @param subType  The type of storage directory to return. Should be one of{@link #TYPE_EXTERNAL_FILE_SUB}. May be null.
         * @return AppSpecificModule
         * @throws IOException
         * @noinspection ResultOfMethodCallIgnored
         */
        public AppSpecificModule targetFile(Object fileName, int type, final String subType) throws Exception {
            mType = type;
            if (type == TYPE_RAW) {
                mFileNameId = (Integer) fileName;
            } else {
                mFileNameStr = (String) fileName;
                if (type > TYPE_LAST_INTERNAL_TYPE && getPrimaryExternalStorage() == null) {
                    throw new IllegalStateException("external storage no mounted");
                }
                switch (type) {
                    case TYPE_DIR_CACHE:
                        mFile = new File(mContext.getCacheDir(), mFileNameStr);
                        break;
                    case TYPE_DIR_FILE:
                        mFile = new File(mContext.getFilesDir(), mFileNameStr);
                        break;
                    case TYPE_DIR_ROOT:
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
                    case TYPE_EXTERNAL_FILE_SUB:
                        mFile = new File(mContext.getExternalFilesDirs(subType)[0], mFileNameStr);
                        break;
                    default:
                        throw new IllegalArgumentException("illegal type");
                }
                if (!mFile.exists()) {
                    mFile.createNewFile();
                }
            }
            return this;
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
            switch (mType) {
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
    }
}
