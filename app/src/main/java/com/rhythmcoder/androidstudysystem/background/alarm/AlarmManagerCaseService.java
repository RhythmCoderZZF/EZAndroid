package com.rhythmcoder.androidstudysystem.background.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.rhythmcoder.baselib.cmd.CmdUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmManagerCaseService extends Service {
    private static final String TAG = "AlarmManagerCaseService";
    private static final String CHANNEL_ID = "alarm_test";

    private NotificationManager mNotificationManager;
    private File mFile;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        mFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "alarm_log.txt");
        if (mFile.exists()) {
            CmdUtil.d(TAG, "file already exists");
            mFile.delete();
        }
        try {
            boolean isCreate = mFile.createNewFile();
            if (isCreate) {
                CmdUtil.e(TAG, "create file");
            } else {
                CmdUtil.e(TAG, "can not create file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "前台服务测试", NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> doBackGroundWork()).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 10 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmManagerCaseService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        String content = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        Notification notification = new Notification.Builder(this, CHANNEL_ID).setContentTitle("这是一条alarm测试通知").setContentText(content).setSmallIcon(com.rhythmcoder.baselib.R.drawable.baseline_info_24).build();
        mNotificationManager.notify(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    public void doBackGroundWork() {
        if (mFile.exists()) {
            int status = -1;
            try {
                // 通过ping百度检测网络是否可用
                Process p = Runtime.getRuntime().exec("/system/bin/ping -c 1 www.baidu.com");
                status = p.waitFor(); // 只有0时表示正常返回
            } catch (IOException e) {
                CmdUtil.e(TAG, "MyService IOException--" + e.getMessage());
                status = -2;
                e.printStackTrace();
            } catch (InterruptedException e) {
                status = -3;
                CmdUtil.e(TAG, "MyService InterruptedException--" + e.getMessage());
                e.printStackTrace();
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(mFile, true));
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String currentDateTime = sdf.format(date);
                CmdUtil.d(TAG, currentDateTime + " status:" + status);
                writer.write(currentDateTime);
                writer.newLine();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            CmdUtil.e(TAG, "file not exist");
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
