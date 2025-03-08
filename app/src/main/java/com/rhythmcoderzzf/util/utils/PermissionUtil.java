package com.rhythmcoderzzf.util.utils;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

import static com.rhythmcoderzzf.util.utils.core.ListenActivityResultFragment.holderFragmentFor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.rhythmcoderzzf.util.utils.core.ListenActivityResultRequest;

public class PermissionUtil {
    private static String HOLDER_TAG = "permission_holder";
    private final Activity context;
    private OnPermissionListener mListener;

    private ListenActivityResultRequest mListenActivityResultRequest;

    public PermissionUtil(FragmentActivity context) {
        this.context = context;
        mListenActivityResultRequest = holderFragmentFor(HOLDER_TAG, context);
    }

    public void requestPermission(String permission, OnPermissionListener listener) {
        mListener = listener;
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            mListener.onPermissionGranted(true);
        } else if (shouldShowRequestPermissionRationale(context, permission)) {
            showInContextUI(permission);
        } else {
            requestPermissions(context, new String[]{permission}, 0);
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
            mListenActivityResultRequest.requestPermissionForResult(new String[]{permission}, 0, (requestCode, permissions, grantResults) -> {
                switch (requestCode) {
                    case 0:
                        // If request is cancelled, the result arrays are empty.
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            mListener.onPermissionGranted(true);
                        } else {
                            if (shouldShowRequestPermissionRationale(context, permissions[0])) {
                                mListener.onPermissionGranted(false);
                            } else {
                                mListener.onPermissionGranted(false);
                            }
                        }
                }
            });
        });
        dialog.show();
    }


    public interface OnPermissionListener {
        void onPermissionGranted(boolean granted);
    }

}
