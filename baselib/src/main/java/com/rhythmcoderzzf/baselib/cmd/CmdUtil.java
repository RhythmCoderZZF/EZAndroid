package com.rhythmcoderzzf.baselib.cmd;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import com.rhythmcoderzzf.baselib.R;
import com.rhythmcoderzzf.baselib.utils.LogUtil;

import java.lang.ref.WeakReference;

/**
 * Author: create by RhythmCoder
 * Date: 2020/6/14
 * Description: cmd浮窗
 */
public class CmdUtil {
    private static final String TAG = "CmdUtil";
    public static FloatViewService.ContentBinder sCmd;//cmd 连接
    private static AlertDialog mOverlayAskDialog;
    private static WeakReference<Context> sContext;

    public static void d(String tag, String s) {
        LogUtil.d(tag, s);
        if (sCmd != null) sCmd.appendContent(R.color.transparent_white, s);
    }

    public static void i(String tag, String s) {
        LogUtil.i(tag, s);
        if (sCmd != null) sCmd.appendContent(R.color.transparent_green, s);

    }

    public static void e(String tag, String s) {
        LogUtil.e(tag, s);
        if (sCmd != null) sCmd.appendContent(R.color.transparent_red, s);
    }

    public static void fps(String fps) {
        if (sCmd != null) sCmd.setFps(fps);
    }

    public static void fps(String fps, @ColorInt int color) {
        if (sCmd != null) sCmd.setFps(fps, color);
    }

    private static void requestPermissionAndBindService() {
        Context context = sContext.get();
        if (RomUtils.checkFloatWindowPermission(context)) {
            context.bindService(new Intent(context, FloatViewService.class), sFloatLogConnection, Context.BIND_AUTO_CREATE);
        } else {
            overlayPermissionRequest(context);
        }
    }

    private static void overlayPermissionRequest(Context context) {
        mOverlayAskDialog = new AlertDialog.Builder(context).setTitle("请求浮窗权限").setMessage("需要开启浮窗权限").setPositiveButton("是", (dialog, which) -> {
            RomUtils.applyPermission(context, () -> {
                new Handler().postDelayed(() -> {
                    if (!RomUtils.checkFloatWindowPermission(context)) {
                        Toast.makeText(context, "未获得浮窗权限", Toast.LENGTH_SHORT).show();
                    } else {
                        //授权成功,bind cmd
                        requestPermissionAndBindService();
                    }
                }, 100);
            });
            mOverlayAskDialog.dismiss();
            mOverlayAskDialog = null;
        }).setCancelable(false).create();
        mOverlayAskDialog.show();
    }

    private static final ServiceConnection sFloatLogConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "onServiceConnected");
            sCmd = (FloatViewService.ContentBinder) service;
            sCmd.showWindow();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected!");
        }
    };

    public static void disConnectCmd(Context context) {
        if (sCmd != null && sContext.get().equals(context)) {
            context.unbindService(sFloatLogConnection);
            sCmd = null;
            sContext = null;
        }
    }

    public static void onActivityResult(int requestCode) {
        RomUtils.onActivityResult(requestCode);
    }

    public static void connectCmdAndShowWindow(Context context) {
        if (sCmd != null) {
            sCmd.showWindow();
        } else {
            sContext = new WeakReference<>(context);
            requestPermissionAndBindService();
        }
    }
}
