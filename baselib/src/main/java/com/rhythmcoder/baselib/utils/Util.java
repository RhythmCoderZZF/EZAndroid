package com.rhythmcoder.baselib.utils;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.rhythmcoder.baselib.R;

import java.lang.ref.WeakReference;

public class Util {
    public static void showCategoryInfo(Context context, View view, String info) {
        WeakReference<View> w = new WeakReference<>(view);
        WeakReference<Context> c = new WeakReference<>(context);
        w.get().setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(c.get());
            builder.setTitle(c.get().getString(R.string.info)).setIcon(ContextCompat.getDrawable(c.get(), R.drawable.baseline_info_24)).setMessage(info).setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss()).create().show();
        });
    }
}
