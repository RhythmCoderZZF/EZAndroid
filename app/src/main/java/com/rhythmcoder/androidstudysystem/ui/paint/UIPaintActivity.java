package com.rhythmcoder.androidstudysystem.ui.paint;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.rhythmcoder.androidstudysystem.R;
import com.rhythmcoder.baselib.BaseActivity;


public class UIPaintActivity extends BaseActivity implements View.OnClickListener {
    private Bitmap bitmap = Bitmap.createBitmap(100, 35, Bitmap.Config.ARGB_8888);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_paint);
        findViewById(R.id.btn_test).setOnClickListener(this);
        findViewById(R.id.btn_portrait).setOnClickListener(this);
        findViewById(R.id.btn_land).setOnClickListener(this);

        bitmap.eraseColor(Color.parseColor("#FFFFFFFF"));
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTextSize(24f);
        paint.setColor(Color.BLACK);
        canvas.drawText("O", 5, 29, paint);
        ((ImageView) findViewById(R.id.iv_test)).setImageBitmap(bitmap);
        int[] pix = new int[bitmap.getWidth() * bitmap.getHeight()];
        calculateSingleColor(bitmap, pix, 0, bitmap.getHeight(), Color.WHITE);
    }

    private static void calculateSingleColor(Bitmap bitmap, int[] pix, int startRow, int endRow, int bgColor) {
        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int index = y * bitmap.getWidth() + x;
                int originColor = bitmap.getPixel(x, y);
                if (originColor != Color.WHITE && originColor != Color.BLACK) {
                    Log.d("zzf", "color:" + originColor);
                } else {
                    Log.d("zzf", "white or black:" + originColor);
                }
                pix[index] = ( originColor== bgColor) ? bgColor : Color.BLACK;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_portrait) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }else if (v.getId() == R.id.btn_land) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

}