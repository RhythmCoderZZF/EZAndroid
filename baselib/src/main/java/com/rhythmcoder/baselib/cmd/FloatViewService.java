package com.rhythmcoder.baselib.cmd;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

import com.rhythmcoder.baselib.R;
import com.rhythmcoder.baselib.utils.LogUtil;


/**
 * Author: create by RhythmCoder
 * Date: 2020/6/12
 * Description: 浮窗service，模拟cmd方式显示浮窗log日志输出
 */
public class FloatViewService extends Service {
    private String TAG = this.getClass().getSimpleName();
    private ContentBinder binder = new ContentBinder();
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;
    private View mFloatingView;
    private TextView tvContent;//日志
    private TextView tvFps;//fps
    private ScrollView scrollView;

    private int mScreenWidth;
    private int mScreenHeight;

    private int mStatusBarHeight;
    private int mNavBarHeight;

    public FloatViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate");
        initWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        //移除FloatingView
        try {
            mWindowManager.removeView(mFloatingView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initWindow() {
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_view, null);
        //获取WindowManager对象
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        //TODO 需要判断StatusBar和NavigationBar是否显示
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            mStatusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int navBarHeightResId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (navBarHeightResId > 0) {
            mNavBarHeight = getResources().getDimensionPixelSize(navBarHeightResId);
        }
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        LogUtil.d(TAG, "Screen w/h:" + mScreenWidth + " " + mScreenHeight + " Status height:" + mStatusBarHeight);
        //设置WindowManger布局参数以及相关属性
        params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android 8.0
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //其他版本
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.format = PixelFormat.RGBA_8888;   //窗口透明
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //初始化位置
        params.gravity = Gravity.TOP | Gravity.START;
        params.width = DensityUtil.dp2px(150);
        params.height = DensityUtil.dp2px(500);
        params.x = mScreenWidth - params.width;
        params.y = 0;
        params.setTitle("MyFloatView");
        initViewAndListener();
    }

    private void initViewAndListener() {
        //内容
        tvContent = mFloatingView.findViewById(R.id.content);
        //fps
        tvFps = mFloatingView.findViewById(R.id.tv_fps);
        scrollView = mFloatingView.findViewById(R.id.scrollview);
        //清空
        mFloatingView.findViewById(R.id.iv_clear).setOnClickListener(view -> {
            tvContent.setText("");
        });
        //隐藏
        mFloatingView.findViewById(R.id.iv_close).setOnClickListener(v -> {
            binder.dismissWindow();
        });
        //拖动
        mFloatingView.findViewById(R.id.iv_move).setOnTouchListener(new View.OnTouchListener() {
            //获取X坐标
            private int windowStartX;
            //获取Y坐标
            private int windowStartY;
            //初始化X的touch坐标
            private float startTouchX;
            //初始化Y的touch坐标
            private float startTouchY;
            private int sliderAlignParentLeft;
            private int sliderAlignParentTop;
            private int sliderHeight;
            private float sliderTouchOffsetX;
            private float sliderTouchOffsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        windowStartX = params.x;
                        windowStartY = params.y;
                        sliderAlignParentLeft = v.getLeft();
                        sliderAlignParentTop = v.getTop();
                        sliderHeight = v.getHeight();
                        startTouchX = event.getRawX();
                        startTouchY = event.getRawY();
                        sliderTouchOffsetX = event.getX();
                        sliderTouchOffsetY = event.getY();
                        LogUtil.d(TAG, "mStatusBarHeight:" + mStatusBarHeight + " mNavHeight:" + mNavBarHeight);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        LogUtil.d(TAG, "x/y:" + event.getX() + " " + event.getY() + " raw:" + event.getRawX() + " " + event.getRawY() + " params:" + params.x + " " + params.y);
                        float rawX = event.getRawX();
                        float rawY = event.getRawY();
                        params.x = windowStartX + (int) (rawX - startTouchX);
                        params.y = windowStartY + (int) (rawY - startTouchY);
                        resolveParamsXY((int) rawX, (int) rawY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }

            private void resolveParamsXY(int rawX, int rawY) {
                if (rawX < sliderAlignParentLeft + sliderTouchOffsetX) {
                    params.x = 0;
                }
                if (mScreenWidth - rawX < (params.width - sliderAlignParentLeft - sliderTouchOffsetX)) {
                    params.x = mScreenWidth - params.width;
                }
                if (rawY < sliderAlignParentTop + sliderTouchOffsetY + mStatusBarHeight) {
                    params.y = 0;
                }
                if (mScreenHeight + mStatusBarHeight - rawY < (sliderHeight - sliderTouchOffsetY)) {
                    params.y = mScreenHeight - params.height;
                }
            }
        });
    }


    /**
     * Service Binder 通信类
     */
    public class ContentBinder extends Binder {
        /**
         * 向cmd添加数据
         *
         * @param s
         */
        public void appendContent(@ColorRes int colorRes, String s) {
            if (mFloatingView == null || mFloatingView.getVisibility() == View.GONE) {
                return;
            }
            mFloatingView.post(() -> {
                SpannableStringBuilder spannable = new SpannableStringBuilder(s);
                spannable.setSpan(new ForegroundColorSpan(getResources().getColor(colorRes)), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                tvContent.append(spannable);
                tvContent.append("\n");

            });
            mFloatingView.postDelayed(() -> {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }, 100);
        }

        public void setFps(String fps, @ColorInt int colorInt) {
            if (mFloatingView != null) mFloatingView.post(() -> {
                tvFps.setText(fps + "fps");
                tvFps.setTextColor(colorInt);
            });
        }

        public void setFps(String fps) {
            if (mFloatingView != null) mFloatingView.post(() -> {
                tvFps.setText(fps + "fps");
                tvFps.setTextColor(Color.GRAY);
            });
        }

        /**
         * 开启 cmd
         */
        public void showWindow() {
            try {
                mWindowManager.addView(mFloatingView, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 隐藏 cmd
         */
        public void dismissWindow() {
            try {
                mWindowManager.removeView(mFloatingView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
