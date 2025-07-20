package com.rhythmcoderzzf.ezandroid.utils;

import android.os.Build;
import android.util.Log;

import com.rhythmcoderzzf.ezandroid.Config;

public class EZLogUtil {
    public static final int V = 0;
    public static final int D = 1;
    public static final int I = 2;
    public static final int W = 3;
    public static final int E = 4;

    public static void v(String tag, String msg) {
        v(tag, msg, null);
    }

    public static void v(String tag, String msg, Throwable t) {
        log(V, tag, msg, t);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public static void d(String tag, String msg, Throwable t) {
        log(D, tag, msg, t);
    }

    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    public static void i(String tag, String msg, Throwable t) {
        log(I, tag, msg, t);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void w(String tag, String msg, Throwable t) {
        log(W, tag, msg, t);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable t) {
        log(E, tag, msg, t);
    }

    private static void log(int level, String tag, String msg, Throwable t) {
        tag = Config.PREFIX_TAG + tag;
        switch (level) {
            case V:
                if (isDebugMode() && Config.EZ_LOG_LEVEL <= V) {
                    Log.v(tag, msg);
                }
                break;
            case D:
                if (Config.EZ_LOG_LEVEL <= D) Log.d(tag, msg, t);
                break;
            case I:
                if (Config.EZ_LOG_LEVEL <= I) Log.i(tag, msg, t);
                break;
            case W:
                if (Config.EZ_LOG_LEVEL <= W) Log.w(tag, msg, t);
                break;
            case E:
                if (Config.EZ_LOG_LEVEL <= E) Log.e(tag, msg, t);
                break;
            default:
                break;
        }
    }

    private static boolean isDebugMode() {
        return !"user".equals(Build.TYPE);
    }
}
