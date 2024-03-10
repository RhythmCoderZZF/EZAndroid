package com.rhythmcoder.androidstudysystem.background.service.fgservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.rhythmcoder.androidstudysystem.background.service.BaseService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ForegroundService extends BaseService {
    private static final String CHANNEL_ID = "channel-001";
    private Thread mBackgroundThread;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private PendingIntent mPendingIntent;
    private boolean mStarted = false;
    private String mNotificationContent = "";

    public ForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationContent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, BackgroundForegroundServiceActivity.class);
        mPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        mNotification = new Notification.Builder(this, CHANNEL_ID).setContentTitle("这是一条前台服务通知").setContentText(mNotificationContent).setSmallIcon(com.rhythmcoder.baselib.R.drawable.baseline_info_24).setContentIntent(mPendingIntent).setTicker("ticker text").build();
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "前台服务测试", NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mStarted) {
            mBackgroundThread = new Thread(() -> {
                mStarted = true;
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    mNotificationContent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                    mNotification = new Notification.Builder(this, CHANNEL_ID).setContentTitle("这是一条前台服务通知").setContentText(mNotificationContent).setSmallIcon(com.rhythmcoder.baselib.R.drawable.baseline_info_24).setContentIntent(mPendingIntent).setTicker("ticker text").build();
                    mNotificationManager.notify(1, mNotification);
                }
                mStarted = false;
            });
            mBackgroundThread.start();
            startForeground(1, mNotification);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBackgroundThread.interrupt();
    }
}