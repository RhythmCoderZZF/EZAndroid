package com.rhythmcoderzzf.ezandroid;

import static com.rhythmcoderzzf.ezandroid.core.ListenActivityResultFragment.holderFragmentFor;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.rhythmcoderzzf.ezandroid.core.ListenActivityResultRequest;

import java.util.Arrays;

public class EZPermission {
    private final String TAG = EZPermission.class.getSimpleName();
    private static String HOLDER_TAG = "permission_holder";
    private final Activity context;
    private OnPermissionListener mListener;

    private ListenActivityResultRequest mListenActivityResultRequest;

    public EZPermission(FragmentActivity context) {
        this.context = context;
        mListenActivityResultRequest = holderFragmentFor(HOLDER_TAG, context);
    }

    public void requestPermission(String[] permissions, @NonNull OnPermissionListener listener) {
        mListener = listener;
        int permissionGrantedCount = 0;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionGrantedCount++;
            }
            /*else if (shouldShowRequestPermissionRationale(context, permission)) {
                showInContextUI(permission);
            }*/
        }
        if (permissions.length != permissionGrantedCount) {
            Log.d(TAG, "start requestPermission,permissions:" + Arrays.toString(permissions));
            mListenActivityResultRequest.requestPermissionForResult(permissions, 0, (requestCode, _permissions, grantResults) -> {
                Log.d(TAG, "requestPermissionForResult,permissions:" + Arrays.toString(_permissions) + " grantResults:" + Arrays.toString(grantResults));
                if (mListener == null) {
                    return;
                }
                // If request is cancelled, the result arrays are empty.
                if (_permissions.length > 0) {
                    int allGrantResult = PackageManager.PERMISSION_GRANTED;
                    for (int grantResult : grantResults) {
                        allGrantResult += grantResult;
                    }
                    if (allGrantResult == PackageManager.PERMISSION_GRANTED) {
                        mListener.onPermissionGranted(true);
                    } else {
                        mListener.onPermissionGranted(false);
                    }
                    return;
                }
                mListener.onPermissionGranted(false);
                /*if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    if (shouldShowRequestPermissionRationale(context, permissions[0])) {
                        mListener.onPermissionGranted(false);
                    } else {
                        mListener.onPermissionGranted(false);
                    }
                }*/
            });
        } else {
            mListener.onPermissionGranted(true);
        }
    }

    public interface OnPermissionListener {
        void onPermissionGranted(boolean granted);
    }

   /* private void showInContextUI(String permission) {
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
    }*/
}
