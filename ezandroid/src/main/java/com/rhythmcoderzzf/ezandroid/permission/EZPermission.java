package com.rhythmcoderzzf.ezandroid.permission;

import static com.rhythmcoderzzf.ezandroid.core.ListenActivityResultFragment.holderFragmentFor;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.rhythmcoderzzf.ezandroid.core.ListenActivityResultRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * EZ权限请求。使用方式：
 *
 * @<code> new EZPermission.Builder(this)
 * .applyRequestPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) </br>
 * .build()</br>
 * .requestPermission((granted, deniedPermissions) -> {</br>
 * if (granted) {</br>
 * //...</br>
 * }</br>
 * });
 * </code>
 */
public class EZPermission {
    private static final String TAG = EZPermission.class.getSimpleName();
    private static final String HOLDER_TAG = "permission_holder";
    private final FragmentActivity mContext;
    private OnPermissionListener mListener;
    private String[] mPermissions;
    /**
     * 使用ActivityResultRequest API 请求权限
     */
    private boolean mNotUseActivityResultAPI = false;
    private ActivityResultLauncher activityResultLauncher;
    private ListenActivityResultRequest mListenActivityResultRequest;

    private EZPermission(FragmentActivity context) {
        this.mContext = context;

    }

    private EZPermission onInit() {
        if (!mNotUseActivityResultAPI) {
            activityResultLauncher = mContext.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onRequestPermissionByUserActivityResultLauncherResult);
        } else mListenActivityResultRequest = holderFragmentFor(HOLDER_TAG, mContext);
        return this;
    }


    /**
     * 请求权限
     *
     * @param listener 回调接口
     */
    public void requestPermission(@NonNull OnPermissionListener listener) {
        int permissionGrantedCount = 0;
        for (String permission : mPermissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionGrantedCount++;
            }
        }
        if (mPermissions.length == permissionGrantedCount) {
            Log.i(TAG, "all permissions already granted");
            listener.onPermissionGranted(true, null);
            return;
        }
        mListener = listener;
        if (!mNotUseActivityResultAPI) {
            requestPermissionByUserActivityResultLauncher();
        } else
            mListenActivityResultRequest.requestPermissionForResult(mPermissions, 0, this::onRequestPermissionResult);
    }

    private void requestPermissionByUserActivityResultLauncher() {
        activityResultLauncher.launch(mPermissions);
    }

    private void onRequestPermissionByUserActivityResultLauncherResult(Map<String, Boolean> allPermissionsMap) {
        Log.d(TAG, "onRequestPermissionByUserActivityResultLauncherResult allPermissionsMap:" + allPermissionsMap);
        //拒绝权限列表
        final List<String> deniedPermissions = new ArrayList<>();
        //永久拒绝权限列表（拒绝且不在提示）
        allPermissionsMap.forEach((permission, granted) -> {
            if (!granted) {
                deniedPermissions.add(permission);
            }
        });
        if (deniedPermissions.isEmpty()) {
            mListener.onPermissionGranted(true, deniedPermissions);
        } else {
            /*final List<String> permanentlyDenied = new ArrayList<>();
            deniedPermissions.forEach(permission -> {
                if (!mContext.shouldShowRequestPermissionRationale(permission)) {
                    permanentlyDenied.add(permission);
                }
            });*/
            mListener.onPermissionGranted(false, deniedPermissions);
        }
    }

    private void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mListener == null) {
            return;
        }
        Log.d(TAG, "onRequestPermissionResult permissions:" + Arrays.toString(permissions) + " grantResults:" + Arrays.toString(grantResults));
        // If request is cancelled, the result arrays are empty.
        if (permissions.length > 0) {
            //拒绝权限列表
            final List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            mListener.onPermissionGranted(deniedPermissions.isEmpty(), deniedPermissions);
        }
    }

    /**
     * 权限申请结果回调监听器
     */
    public interface OnPermissionListener {
        /**
         * 权限申请结果回调函数。
         *
         * @param granted           true:所有权限都已申请;false:某些权限被用户拒绝
         * @param deniedPermissions 当granted = false时，被用户拒绝的权限名称数组
         */
        void onPermissionGranted(boolean granted, List<String> deniedPermissions);
    }

    public static class Builder {
        private boolean tmpApplyNotUseActivityResultAPI;
        private String[] tmpPermissions;
        private FragmentActivity tmpContext;

        public Builder(FragmentActivity context) {
            tmpContext = context;
        }

        /**
         * 是否使用ActivityResultLauncher API来请求权限
         *
         * @param applyUseActivityResultLauncher 是否使用ActivityResultLauncher API
         * @return EZPermission
         */
        public Builder applyUseActivityResultApi(boolean applyUseActivityResultLauncher) {
            tmpApplyNotUseActivityResultAPI = !applyUseActivityResultLauncher;
            return this;
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
            ezPermission.mNotUseActivityResultAPI = tmpApplyNotUseActivityResultAPI;
            ezPermission.mPermissions = tmpPermissions;
            return ezPermission.onInit();
        }
    }
}
