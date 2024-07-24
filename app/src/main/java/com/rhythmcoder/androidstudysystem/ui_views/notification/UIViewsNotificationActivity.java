package com.rhythmcoder.androidstudysystem.ui_views.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;


public class UIViewsNotificationActivity extends BaseActivity implements View.OnClickListener {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_id_01";
    private static final String CHANNEL_NAME = "测试Channel";
    private static final String CHANNEL_DESCRIPTION = "用来学习通知相关的API";

    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_views_notification);
        findViewById(R.id.btn_start).setOnClickListener(this);
        createNotificationChannel();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            new Handler().postDelayed(() -> {
                showNotification();
            }, 5000);
        }
    }

    private void createNotificationChannel() {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("通知测试").setContentText("通知测试 注意指示灯亮~").setAutoCancel(true);  // 点击后自动取消通知
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}