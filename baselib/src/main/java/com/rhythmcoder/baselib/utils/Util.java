package com.rhythmcoder.baselib.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.rhythmcoder.baselib.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class Util {
    private static final String TAG = "Util";

    public static void showCategoryInfo(Context context, View view, String info) {
        WeakReference<View> w = new WeakReference<>(view);
        WeakReference<Context> c = new WeakReference<>(context);
        w.get().setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(c.get());
            builder.setTitle(c.get().getString(R.string.info)).setIcon(ContextCompat.getDrawable(c.get(), R.drawable.baseline_info_24)).setMessage(info).setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss()).create().show();
        });
    }

    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            LogUtil.i(TAG, "exeCmd:"+cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                LogUtil.d(TAG,"result:"+ line);
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String exeCmd(String c) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            String[] cmd = {"/system/bin/sh", "-c", c};
            Process p = Runtime.getRuntime().exec(cmd);
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            LogUtil.i(TAG, "exeCmd:"+cmd);
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                LogUtil.d(TAG, "result:"+line);
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
