package com.rhythmcoder.baselib.utils;

import android.util.Log;

/**
 * Author:create by RhythmCoderZZF
 * Date:2023/12/16
 * Description:
 */
public class LogUtil {
    private static final String MAINTAG = "rczzf";
    private static final int D = 1;
    private static final int I = 2;
    private static final int W = 3;
    private static final int E = 4;

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
        tag = MAINTAG + "/" + tag;
        switch (level) {
            case D:
                Log.d(tag, msg, t);
                break;
            case I:
                Log.i(tag, msg, t);
                break;
            case W:
                Log.w(tag, msg, t);
                break;
            case E:
                Log.e(tag, msg, t);
                break;
            default:
                break;
        }
    }
}
