package com.rhythmcoder.androidstudysystem.background.service.fgservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.rhythmcoder.androidstudysystem.background.service.BaseService;
import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ForegroundService extends BaseService {
    private static final String CHANNEL_ID = "channel-001";
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private PendingIntent mPendingIntent;
    private String mNotificationContent = "";
    private Handler mHandler = new Handler();

    public ForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "前台服务测试", NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(notificationChannel);
        Intent notificationIntent = new Intent(this, BackgroundForegroundServiceActivity.class);
        mPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNotificationContent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        mNotification = new Notification.Builder(this, CHANNEL_ID).setContentTitle("这是一条前台服务通知").setContentText(mNotificationContent).setSmallIcon(com.rhythmcoder.baselib.R.drawable.baseline_info_24).setContentIntent(mPendingIntent).setTicker("ticker text").build();
        CmdUtil.d(TAG, "startForeground<<");
        //核心方法：将当前服务转为前台服务，需要传入Notification对象
        startForeground(1, mNotification);
        mHandler.postDelayed(() -> {
            CmdUtil.d(TAG, "stopForeground<<");
            //移除前台服务状态
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        }, 5000);
        return super.onStartCommand(intent, flags, startId);
    }
}