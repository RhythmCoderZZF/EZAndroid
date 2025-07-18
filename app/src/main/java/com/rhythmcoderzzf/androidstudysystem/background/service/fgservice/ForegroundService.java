package com.rhythmcoderzzf.androidstudysystem.background.service.fgservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.rhythmcoderzzf.androidstudysystem.background.service.BaseService;
import com.rhythmcoderzzf.baselib.cmd.CmdUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ForegroundService extends BaseService {
    private static final String CHANNEL_ID = "channel-001";
    private PendingIntent mPendingIntent;
    private Handler mHandler = new Handler();

    public ForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "前台服务测试", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(notificationChannel);
        Intent notificationIntent = new Intent(this, BackgroundForegroundServiceActivity.class);
        mPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String content = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        Notification notification = new Notification.Builder(this, CHANNEL_ID).setContentTitle("这是一条前台服务通知").setContentText(content).setSmallIcon(com.rhythmcoderzzf.baselib.R.drawable.baseline_info_24).setContentIntent(mPendingIntent).setTicker("ticker text").build();
        CmdUtil.d(TAG, "startForeground<<");
        //核心方法：将当前服务转为前台服务，需要传入Notification对象
        startForeground(1, notification);
        mHandler.postDelayed(() -> {
            CmdUtil.d(TAG, "stopForeground<<");
            //移除前台服务状态
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        }, 5000);
        return super.onStartCommand(intent, flags, startId);
    }
}