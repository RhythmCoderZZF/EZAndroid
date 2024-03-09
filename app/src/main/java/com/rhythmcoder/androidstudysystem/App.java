package com.rhythmcoder.androidstudysystem;

import android.app.Application;
import android.graphics.Color;
import android.view.Choreographer;

import com.rhythmcoder.baselib.cmd.CmdUtil;

public class App extends Application {

    private long MONITOR_INTERVAL = 160L;
    private long MONITOR_INTERVAL_NANOS = MONITOR_INTERVAL * 1000L * 1000L;

    /**
     * 设置计算fps的单位时间间隔1000ms,即fps/s;
     */
    private long MAX_INTERVAL = 1000L;

    private long mStartFrameTime;
    private int mFrameCount;
    private Choreographer.FrameCallback mCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        initFPS();
    }

    private void initFPS() {
        mCallback = frameTimeNanos -> {
            if (mStartFrameTime == 0L) {
                mStartFrameTime = frameTimeNanos;
            }
            long interval = frameTimeNanos - mStartFrameTime;
            if (interval > MONITOR_INTERVAL_NANOS) {
                double fps = (double) ((mFrameCount * 1000L * 1000L)) / interval * MAX_INTERVAL;
                int fpsInt = (int) fps;
                String fps1 = String.valueOf(fpsInt);

                if (fpsInt < 30) {
                    CmdUtil.fps(fps1, Color.RED);
                } else if (fpsInt < 60) {
                    CmdUtil.fps(fps1);
                } else {
                    CmdUtil.fps(fps1, Color.GREEN);
                }
                mFrameCount = 0;
                mStartFrameTime = 0;
            } else {
                ++mFrameCount;
            }
            Choreographer.getInstance().postFrameCallback(mCallback);
        };
        Choreographer.getInstance().postFrameCallback(mCallback);
    }
}
