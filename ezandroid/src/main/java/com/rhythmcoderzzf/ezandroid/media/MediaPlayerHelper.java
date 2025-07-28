package com.rhythmcoderzzf.ezandroid.media;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import java.io.IOException;

public class MediaPlayerHelper {
    private static final String TAG = "MediaPlayerHelper";
    private static MediaPlayerHelper instance;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private OnStateChangeListener stateListener;

    // 单例模式
    public static synchronized MediaPlayerHelper getInstance() {
        if (instance == null) {
            instance = new MediaPlayerHelper();
        }
        return instance;
    }

    private MediaPlayerHelper() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        setupListeners();
    }

    // 设置监听器
    private void setupListeners() {
        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            if (stateListener != null) {
                stateListener.onPrepared(mp.getDuration());
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            if (stateListener != null) {
                stateListener.onCompletion();
            }
            release(); // 播放完成自动释放资源
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Playback Error: " + what + ", " + extra);
            if (stateListener != null) {
                stateListener.onError("Playback failed: Code " + what);
            }
            return true; // 消费错误事件
        });
    }

    /**
     * 设置数据源（支持本地路径/网络URL）
     * @param path 文件路径或URL
     */
    public void setDataSource(String path) {
        reset(); // 重置播放器状态
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync(); // 异步准备（避免阻塞主线程）
        } catch (IOException e) {
            Log.e(TAG, "setDataSource Error: ", e);
            if (stateListener != null) {
                stateListener.onError("Invalid data source");
            }
        }
    }

    /**
     * 绑定视频渲染视图（TextureView）
     * @param textureView 用于显示视频的TextureView
     */
    public void bindVideoView(TextureView textureView) {
        if (textureView.isAvailable()) {
            mediaPlayer.setSurface(new Surface(textureView.getSurfaceTexture()));
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    mediaPlayer.setSurface(new Surface(surface));
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

                }
            });
        }
    }

    // ------------------------- 播放控制 -------------------------
    public void start() {
        if (isPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (stateListener != null) {
                stateListener.onPlaying();
            }
        }
    }

    public void pause() {
        if (isPrepared && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (stateListener != null) {
                stateListener.onPaused();
            }
        }
    }

    public void stop() {
        if (isPrepared) {
            mediaPlayer.stop();
            isPrepared = false;
            if (stateListener != null) {
                stateListener.onStopped();
            }
        }
    }

    /**
     * 跳转到指定位置
     * @param position 毫秒数
     */
    public void seekTo(int position) {
        if (isPrepared) {
            mediaPlayer.seekTo(position);
        }
    }

    // ------------------------- 资源管理 -------------------------
    public void reset() {
        mediaPlayer.reset();
        isPrepared = false;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            instance = null; // 释放单例
        }
    }

    // ------------------------- 状态监听接口 -------------------------
    public interface OnStateChangeListener {
        void onPrepared(int totalDuration); // 准备完成
        void onPlaying();                   // 开始播放
        void onPaused();                    // 暂停
        void onStopped();                   // 停止
        void onCompletion();                // 播放完成
        void onError(String message);        // 错误回调
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.stateListener = listener;
    }

    // ------------------------- 实用方法 -------------------------
    public int getCurrentPosition() {
        return isPrepared ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return isPrepared ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}