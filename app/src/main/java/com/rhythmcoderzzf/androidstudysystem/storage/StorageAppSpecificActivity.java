package com.rhythmcoderzzf.androidstudysystem.storage;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityStorageAppSpcificBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.storage.EZStorage;
import com.rhythmcoderzzf.ezandroid.utils.EZFileUtil;


public class StorageAppSpecificActivity extends BaseActivity<ActivityStorageAppSpcificBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.btnInternalCache.setOnClickListener(this::internalCache);
        mBinding.btnInternalFile.setOnClickListener(this::internalFile);
        mBinding.btnExternalCache.setOnClickListener(this::externalCache);
        mBinding.btnExternalFile.setOnClickListener(this::externalFile);
    }

    @Override
    protected ActivityStorageAppSpcificBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityStorageAppSpcificBinding.inflate(layoutInflater);
    }

    private void internalCache(View view) {
        EZStorage ezStorage = null;
        try {
            ezStorage = new EZStorage.Builder(this)
                    .applyFileName("temp.txt")
                    .applyFileType(EZStorage.TYPE_INTERNAL_DIR_CACHE)
                    .build();
            EZFileUtil.writeText(ezStorage.getOutputStream(), "hello\r\n", false);
            toast("写成功" + ezStorage.getFile().getAbsolutePath());
            String content = EZFileUtil.readText(ezStorage.getInputStream());
            toast("读取数据：" + content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ezStorage != null) {
                ezStorage.release();
            }
        }
    }

    private void internalFile(View view) {
        EZStorage tempModule = null;
        EZStorage module = null;

        try {
            tempModule = new EZStorage.Builder(this).applyFileName("temp.txt").applyFileType(EZStorage.TYPE_INTERNAL_DIR_CACHE).build();
            EZFileUtil.writeText(tempModule.getOutputStream(), "hello Jack!\r\n", false);

            module = new EZStorage.Builder(this).applyFileName("copy_temp.txt").applyFileType(EZStorage.TYPE_INTERNAL_DIR_FILE).build();
            EZFileUtil.copyStream(tempModule.getInputStream(), module.getOutputStream());
            toast("copy成功" + module.getFile().getAbsolutePath());
            String content = EZFileUtil.readText(module.getInputStream());
            toast("读取数据：" + content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (module != null) {
                tempModule.release();
                module.release();
            }
        }
    }

    private void externalCache(View view) {
        EZStorage module = null;
        try {
            module = new EZStorage.Builder(this).applyFileName("temp.txt").applyFileType(EZStorage.TYPE_EXTERNAL_CACHE).build();
            EZFileUtil.writeText(module.getOutputStream(), "I'm Jack!\r\n", false);
            toast("写成功" + module.getFile().getAbsolutePath());
            String content = EZFileUtil.readText(module.getInputStream());
            toast("读取数据：" + content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (module != null) {
                module.release();
            }
        }
    }

    private void externalFile(View view) {
        EZStorage tempModule = null;
        EZStorage module = null;
        try {
            tempModule = new EZStorage.Builder(this).applyFileName("temp.txt").applyFileType(EZStorage.TYPE_EXTERNAL_CACHE).build();
            EZFileUtil.writeText(tempModule.getOutputStream(), "Hello I'm Jack!\r\n", false);

            module = new EZStorage.Builder(this).applyFileName("copy_temp.txt").applyFileType(EZStorage.TYPE_EXTERNAL_FILE_SUB_MUSIC).build();
            EZFileUtil.copyStream(tempModule.getInputStream(), module.getOutputStream());
            toast("copy成功" + module.getFile().getAbsolutePath());
            String content = EZFileUtil.readText(module.getInputStream());
            toast("读取数据：" + content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (module != null) {
                tempModule.release();
                module.release();
            }
        }
    }
}