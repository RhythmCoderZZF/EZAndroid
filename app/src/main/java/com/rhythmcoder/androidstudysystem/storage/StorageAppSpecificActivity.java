package com.rhythmcoder.androidstudysystem.storage;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.rhythmcoder.androidstudysystem.databinding.ActivityStorageAppSpcificBinding;
import com.rhythmcoder.baselib.BaseActivity;
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
        EZStorage.AppSpecificModule module = null;
        try {
            module = EZStorage.getInstance(this).loadAppSpecific().targetFile("temp.txt", EZStorage.AppSpecificModule.TYPE_DIR_CACHE, null);
            EZFileUtil.writeText(module.getOutputStream(), "hello\r\n", false);
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

    private void internalFile(View view) {
        EZStorage.AppSpecificModule tempModule = null;
        EZStorage.AppSpecificModule module = null;
        try {
            tempModule = EZStorage.getInstance(this).loadAppSpecific().targetFile("temp.txt", EZStorage.AppSpecificModule.TYPE_DIR_CACHE, null);
            EZFileUtil.writeText(tempModule.getOutputStream(), "hello Jack!\r\n", false);

            module = EZStorage.getInstance(this).loadAppSpecific().targetFile("copy_temp.txt", EZStorage.AppSpecificModule.TYPE_DIR_FILE, null);
            EZFileUtil.copyStream(tempModule.getInputStream(), module.getOutputStream());
            toast("copy成功"+ module.getFile().getAbsolutePath());
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
        EZStorage.AppSpecificModule module = null;
        try {
            module = EZStorage.getInstance(this).loadAppSpecific().targetFile("temp.txt", EZStorage.AppSpecificModule.TYPE_EXTERNAL_CACHE, null);
            EZFileUtil.writeText(module.getOutputStream(), "I'm Jack!\r\n", false);
            toast("写成功"+ module.getFile().getAbsolutePath());
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
        EZStorage.AppSpecificModule tempModule = null;
        EZStorage.AppSpecificModule module = null;
        try {
            tempModule = EZStorage.getInstance(this).loadAppSpecific().targetFile("temp.txt", EZStorage.AppSpecificModule.TYPE_EXTERNAL_CACHE, null);
            EZFileUtil.writeText(tempModule.getOutputStream(), "Hello I'm Jack!\r\n", false);

            module = EZStorage.getInstance(this).loadAppSpecific().targetFile("copy_temp.txt", EZStorage.AppSpecificModule.TYPE_EXTERNAL_FILE_SUB, Environment.DIRECTORY_MUSIC);
            EZFileUtil.copyStream(tempModule.getInputStream(), module.getOutputStream());
            toast("copy成功"+ module.getFile().getAbsolutePath());
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