package com.rhythmcoderzzf.androidstudy.permission;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rhythmcoderzzf.androidstudy.R;
import com.rhythmcoderzzf.baselib.BaseActivity;

public class PermissionMainActivity extends BaseActivity implements View.OnClickListener {

    private static final String PERMISSION = Manifest.permission.READ_CONTACTS;

    private static final String PERMISSION2 = Manifest.permission.ACCESS_FINE_LOCATION;

    private Button mBtnRequestPermission;
    private Button mBtnRequestPermission2;

    private boolean mApi2 = false;

    //request one permission
    private ActivityResultLauncher<String> mRequestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            toastPermissionIsGranted(PERMISSION2);
        } else {
            if (shouldShowRequestPermissionRationale(PERMISSION2)) {
                toastTellCustomerWeNeedPermission(PERMISSION2);
            } else {
                toastCustomerNeverAllowPermission(PERMISSION2);
            }
        }
    });

    //request multiple permissions
    /*private ActivityResultLauncher<String[]> mRequestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        result.forEach((s, aBoolean) -> {});
    });*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        mBtnRequestPermission = findViewById(R.id.btnRequestPermission);
        mBtnRequestPermission.setOnClickListener(this);
        mBtnRequestPermission2 = findViewById(R.id.btnRequestPermission2);
        mBtnRequestPermission2.setOnClickListener(this);
        //Util.showCategoryInfo(this, findViewById(R.id.tvCategory1), "lalala");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRequestPermission) {
            mApi2 = false;
            checkHasPermission(PERMISSION);
        } else if (v.getId() == R.id.btnRequestPermission2) {
            mApi2 = true;
            checkHasPermission(PERMISSION2);
        }
    }

    private void checkHasPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            toastAlreadyGetPermission(permission);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showInContextUI(permission);
        } else {
            if (mApi2) {
                requestPermissionApi2(permission);
            } else {
                requestPermissionApi1(permission);
            }
        }
    }

    private void requestPermissionApi1(String permission) {
        requestPermissions(new String[]{permission}, 0);
    }

    private void requestPermissionApi2(String permission) {
        mRequestPermissionLauncher.launch(permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toastPermissionIsGranted(permissions[0]);
                } else {
                    if (shouldShowRequestPermissionRationale(permissions[0])) {
                        toastTellCustomerWeNeedPermission(permissions[0]);
                    } else {
                        toastCustomerNeverAllowPermission(permissions[0]);
                    }
                }
        }
    }


    private void showInContextUI(String permission) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Attention");
        dialog.setMessage("We need permission to Continue the action or workflow in this app !!");
        dialog.setNegativeButton("cancel", (d, which) -> {
            d.dismiss();
        });
        dialog.setPositiveButton("ok", (dialog1, which) -> {
            dialog1.dismiss();
            if (mApi2) {
                requestPermissionApi2(permission);
            } else {
                requestPermissionApi1(permission);
            }

        });
        dialog.show();
    }

    private void toastPermissionIsGranted(String permission) {
        toast("request " + permission.substring(permission.lastIndexOf(".")) + " success ...");
    }

    private void toastTellCustomerWeNeedPermission(String permission) {
        toast("we need " + permission.substring(permission.lastIndexOf(".")) + "\n to use this feature !!");
    }

    private void toastAlreadyGetPermission(String permission) {
        toast(permission.substring(permission.lastIndexOf(".")) + " already granted ~~");
    }

    private void toastCustomerNeverAllowPermission(String permission) {
        toast("user are not allowed to grant " + permission.substring(permission.lastIndexOf(".")));
    }
}