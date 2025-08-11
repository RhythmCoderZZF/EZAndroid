package com.rhythmcoderzzf.ezandroid.permission;

import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.rhythmcoderzzf.ezandroid.core.AbstractBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EZ 权限请求。
 * 使用方式：
 * <pre>
 *     new EZPermission.Builder(this)
 *              .applyRequestPermission(Manifest.permission.CAMERA,...)
 *              .build()
 *              .requestPermission((deniedPermissions) -> {
 *                  if (deniedPermissions.isEmpty()) {
 *                      //所有权限被授权
 *                   }
 *              });
 * </pre>
 */
public class EZPermission {
    private static final String TAG = EZPermission.class.getSimpleName();
    private final FragmentActivity mContext;
    private OnPermissionListener mListener;
    private String[] mPermissions;
    private ActivityResultLauncher<String[]> activityResultLauncher;

    private EZPermission(FragmentActivity context) {
        this.mContext = context;
    }

    private EZPermission onInit() {
        activityResultLauncher = mContext.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onRequestPermissionByUserActivityResultLauncherResult);
        return this;
    }


    /**
     * 请求权限
     *
     * @param listener 回调接口
     */
    public void requestPermission(@Nullable OnPermissionListener listener) {
        mListener = listener;
        int permissionGrantedCount = 0;
        for (String permission : mPermissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionGrantedCount++;
            }
        }
        if (mPermissions.length == permissionGrantedCount && mListener != null) {
            //权限已经都被授权过
            mListener.onPermissionGranted(new ArrayList<>());
            return;
        }
        activityResultLauncher.launch(mPermissions);
    }

    private void onRequestPermissionByUserActivityResultLauncherResult(Map<String, Boolean> allPermissionsMap) {
        //用户拒绝的权限列表
        final List<String> deniedPermissions = new ArrayList<>();
        //永久拒绝权限列表（拒绝且不在提示）
        allPermissionsMap.forEach((permission, granted) -> {
            if (!granted) {
                deniedPermissions.add(permission);
            }
        });
        if (deniedPermissions.isEmpty()) {
            if (mListener != null) mListener.onPermissionGranted(deniedPermissions);
        } else {
            /*final List<String> permanentlyDenied = new ArrayList<>();
            deniedPermissions.forEach(permission -> {
                if (!mContext.shouldShowRequestPermissionRationale(permission)) {
                    permanentlyDenied.add(permission);
                }
            });*/
            if (mListener != null) mListener.onPermissionGranted(deniedPermissions);
        }
    }

    /**
     * 权限申请结果回调监听器
     */
    public interface OnPermissionListener {
        /**
         * 权限申请结果回调函数。
         *
         * @param deniedPermissions 被用户拒绝的权限名称数组
         */
        void onPermissionGranted(List<String> deniedPermissions);
    }

    public static class Builder implements AbstractBuilder<EZPermission> {
        private String[] tmpPermissions;
        private FragmentActivity tmpContext;

        public Builder(FragmentActivity context) {
            tmpContext = context;
        }

        /**
         * 设置需要请求的动态权限
         *
         * @param permissions 动态权限
         * @return EZPermission
         */
        public Builder applyRequestPermission(String... permissions) {
            tmpPermissions = permissions;
            return this;
        }

        public EZPermission build() {
            EZPermission ezPermission = new EZPermission(tmpContext);
            ezPermission.mPermissions = tmpPermissions;
            return ezPermission.onInit();
        }
    }
}
