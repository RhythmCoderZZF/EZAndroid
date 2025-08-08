package com.rhythmcoderzzf.androidstudysystem.media;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityMediaProjectionRecordAudioBinding;
import com.rhythmcoderzzf.ezandroid.permission.EZPermission;
import com.rhythmcoderzzf.ezandroid.utils.EZLogUtil;

import java.io.FileOutputStream;
import java.util.concurrent.Executors;

public class MediaProjectionRecordAudioActivity extends BaseActivity<ActivityMediaProjectionRecordAudioBinding> {
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;

    private AudioRecord audioRecord;
    private boolean mRunning;
    @SuppressLint("MissingPermission")
    ActivityResultLauncher<Intent> startMediaProjection = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mediaProjection = mediaProjectionManager.getMediaProjection(result.getResultCode(), result.getData());
            AudioPlaybackCaptureConfiguration audioPlaybackCaptureConfiguration = new AudioPlaybackCaptureConfiguration.Builder(mediaProjection).addMatchingUsage(AudioAttributes.USAGE_MEDIA) //内录音乐声音
                    .addMatchingUsage(AudioAttributes.USAGE_GAME) //内录游戏声音
                    .build();

            int bufferSize = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            AudioFormat audioFormat = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(48000).setChannelMask(AudioFormat.CHANNEL_IN_STEREO).build();
            Executors.newSingleThreadExecutor().submit(() -> {
                audioRecord = new AudioRecord.Builder().setAudioFormat(audioFormat).setBufferSizeInBytes(bufferSize).setAudioPlaybackCaptureConfig(audioPlaybackCaptureConfiguration).build();
                audioRecord.startRecording();
                try (FileOutputStream fos = new FileOutputStream(getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/record.pcm")) {
                    byte[] byteBuffer = new byte[bufferSize];
                    while (mRunning) {
                        int size = audioRecord.read(byteBuffer, 0, byteBuffer.length);
                        if (size >= 0) {
                            fos.write(byteBuffer, 0, size);
                        }
                        EZLogUtil.d(TAG, "写入音频数据size：" + size);
                    }
                    audioRecord.stop();
                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new EZPermission.Builder(this).applyRequestPermission(Manifest.permission.RECORD_AUDIO).build().requestPermission((deniedPermissions) -> {

        });
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mBinding.cbRecord.setOnClickListener(v -> {
            mRunning = !mRunning;
            if (mRunning) {
                startScreenCapture();
            }
        });
    }

    @Override
    protected ActivityMediaProjectionRecordAudioBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityMediaProjectionRecordAudioBinding.inflate(layoutInflater);
    }

    private void startScreenCapture() {
        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
    }
}