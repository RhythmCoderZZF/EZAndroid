package com.rhythmcoder.androidstudysystem.permission;

import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtil2 {
    private final Context context;
    private final String permission;
    private final OnPermissionListener mListener;
    private ActivityResultLauncher<String> mRequestPermissionLauncher;

    public PermissionUtil2(Context context, String permission, OnPermissionListener listener) {
        this.context = context;
        this.permission = permission;
        mListener = listener;

        mRequestPermissionLauncher = ((ComponentActivity) context).registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                toastPermissionIsGranted(permission);
                if (mListener != null) mListener.onPermissionGranted(true);
            } else {
                if (shouldShowRequestPermissionRationale((Activity) context, permission)) {
                    toastTellCustomerWeNeedPermission(permission);
                    if (mListener != null) mListener.onPermissionGranted(false);
                } else {
                    toastCustomerNeverAllowPermission(permission);
                    if (mListener != null) mListener.onPermissionGranted(false);
                }
            }
        });
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            toastAlreadyGetPermission(permission);
            if (mListener != null) mListener.onPermissionGranted(true);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
            showInContextUI(permission);
        } else {
            mRequestPermissionLauncher.launch(permission);
        }
    }


    private void showInContextUI(String permission) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Attention");
        dialog.setMessage("We need permission to Continue the action or workflow in this app !!");
        dialog.setCancelable(false);
        dialog.setNegativeButton("cancel", (d, which) -> {
            d.dismiss();
            if (mListener != null) mListener.onPermissionGranted(false);
        });
        dialog.setPositiveButton("ok", (dialog1, which) -> {
            dialog1.dismiss();
            mRequestPermissionLauncher.launch(permission);
        });
        dialog.show();
    }

    private void toastPermissionIsGranted(String permission) {
        Toast.makeText(context, "request " + permission.substring(permission.lastIndexOf(".")) + " success ...", Toast.LENGTH_SHORT).show();
    }

    private void toastTellCustomerWeNeedPermission(String permission) {
        Toast.makeText(context, "we need " + permission.substring(permission.lastIndexOf(".")) + "\n to use this feature !!", Toast.LENGTH_SHORT).show();
    }

    private void toastAlreadyGetPermission(String permission) {
        Toast.makeText(context, permission.substring(permission.lastIndexOf(".")) + " already granted ~~", Toast.LENGTH_SHORT).show();
    }

    private void toastCustomerNeverAllowPermission(String permission) {
        Toast.makeText(context, "user are not allowed to grant " + permission.substring(permission.lastIndexOf(".")), Toast.LENGTH_SHORT).show();
    }

    public interface OnPermissionListener {
        void onPermissionGranted(boolean granted);
    }

}
