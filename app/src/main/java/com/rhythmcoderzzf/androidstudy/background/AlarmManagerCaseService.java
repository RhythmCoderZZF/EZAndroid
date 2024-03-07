package com.rhythmcoderzzf.androidstudy.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;

import com.rhythmcoderzzf.baselib.cmd.CmdUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmManagerCaseService extends Service {
    private static final String TAG = "AlarmManagerCaseService";
    private File mFile;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        netWorkIsEnable();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmManagerCaseService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    public void netWorkIsEnable() {
        int status = -1;

        try {
            // 通过ping百度检测网络是否可用
            Process p = Runtime.getRuntime().exec("/system/bin/ping -c " + 1 + " 202.108.22.5");
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
        if (mFile.exists()) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(mFile, true));
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String currentDateTime = sdf.format(date) + "--status--" + status;
                CmdUtil.d(TAG, currentDateTime);
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
