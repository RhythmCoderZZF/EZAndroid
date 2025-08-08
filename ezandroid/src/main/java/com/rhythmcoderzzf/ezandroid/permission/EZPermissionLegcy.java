package com.rhythmcoderzzf.ezandroid.permission;

import static com.rhythmcoderzzf.ezandroid.core.ListenActivityResultFragment.holderFragmentFor;

import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.rhythmcoderzzf.ezandroid.core.AbstractBuilder;
import com.rhythmcoderzzf.ezandroid.core.ListenActivityResultRequest;

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
 *              .requestPermission((granted, deniedPermissions) -> {
 *                  if (granted) {
 *                      //...
 *                   }
 *              });
 * </pre>
 */
public class EZPermissionLegcy {
    private static final String TAG = EZPermissionLegcy.class.getSimpleName();
    private static final String HOLDER_TAG = "permission_holder";
    private final FragmentActivity mContext;
    private OnPermissionListener mListener;
    private String[] mPermissions;
    /**
     * 默认使用ActivityResultRequest API 请求权限
     */
    private boolean mNotUseActivityResultAPI = false;
    private ActivityResultLauncher<String[]> activityResultLauncher;
    private ListenActivityResultRequest mListenActivityResultRequest;

    private EZPermissionLegcy(FragmentActivity context) {
        this.mContext = context;
    }

    private EZPermissionLegcy onInit() {
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
    public void requestPermission(@Nullable OnPermissionListener listener) {
        mListener = listener;
        int permissionGrantedCount = 0;
        for (String permission : mPermissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionGrantedCount++;
            }
        }
        if (mPermissions.length == permissionGrantedCount && mListener != null) {
            mListener.onPermissionGranted( new ArrayList<>());
            return;
        }
        if (!mNotUseActivityResultAPI) {
            requestPermissionByActivityResultLauncher();
        } else
            mListenActivityResultRequest.requestPermissionForResult(mPermissions, 0, this::onRequestPermissionResult);
    }

    private void requestPermissionByActivityResultLauncher() {
        activityResultLauncher.launch(mPermissions);
    }

    private void onRequestPermissionByUserActivityResultLauncherResult(Map<String, Boolean> allPermissionsMap) {
        //拒绝权限列表
        final List<String> deniedPermissions = new ArrayList<>();
        //永久拒绝权限列表（拒绝且不在提示）
        allPermissionsMap.forEach((permission, granted) -> {
            if (!granted) {
                deniedPermissions.add(permission);
            }
        });
        if (deniedPermissions.isEmpty()) {
            if (mListener != null) mListener.onPermissionGranted( deniedPermissions);
        } else {
            /*final List<String> permanentlyDenied = new ArrayList<>();
            deniedPermissions.forEach(permission -> {
                if (!mContext.shouldShowRequestPermissionRationale(permission)) {
                    permanentlyDenied.add(permission);
                }
            });*/
            if (mListener != null) mListener.onPermissionGranted( deniedPermissions);
        }
    }

    private void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0) {
            //拒绝权限列表
            final List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (mListener != null)
                mListener.onPermissionGranted(deniedPermissions);
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

    public static class Builder implements AbstractBuilder<EZPermissionLegcy> {
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

        public EZPermissionLegcy build() {
            EZPermissionLegcy ezPermission = new EZPermissionLegcy(tmpContext);
            ezPermission.mNotUseActivityResultAPI = tmpApplyNotUseActivityResultAPI;
            ezPermission.mPermissions = tmpPermissions;
            return ezPermission.onInit();
        }
    }
}
