package com.zzf.ezandroid.core;

import android.content.Intent;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ListenActivityResultFragment extends Fragment implements ListenActivityResultRequest {
    private static String HOLDER_TAG = "";
    private SparseArray<ListenActivityResultRequest.OnActivityResultCallBack> activityResultCallbackMap;
    private ListenActivityResultRequest.OnPermissionResultCallback onPermissionResultCallback;
    private ListenActivityResultRequest.OnLifecycleCallback onLifecycleCallback;

    @Override
    public void registerLifecycleListener(ListenActivityResultRequest.OnLifecycleCallback callback) {
        onLifecycleCallback = callback;
    }
    @Override
    public void startActivityForResult(Intent intent, int requestCode, ListenActivityResultRequest.OnActivityResultCallBack callBack) {
        if (activityResultCallbackMap == null) {
            activityResultCallbackMap = new SparseArray<>();
        }
        activityResultCallbackMap.put(requestCode, callBack);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void requestPermissionForResult(String[] permissions, int requestCode, OnPermissionResultCallback callback) {
        onPermissionResultCallback = callback;
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (onPermissionResultCallback != null) {
            onPermissionResultCallback.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ListenActivityResultRequest.OnActivityResultCallBack callBack = activityResultCallbackMap.get(requestCode);
        if (callBack != null) {
            callBack.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (onLifecycleCallback != null) {
            onLifecycleCallback.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (onLifecycleCallback != null) {
            onLifecycleCallback.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (onLifecycleCallback != null) {
            onLifecycleCallback.onDestroy();
        }
    }

    public static ListenActivityResultRequest holderFragmentFor(String tag, FragmentActivity activity) {
        HOLDER_TAG = tag;
        return holderFragmentFor(activity.getSupportFragmentManager());
    }

    public static ListenActivityResultRequest holderFragmentFor(String tag, Fragment fragment) {
        HOLDER_TAG = tag;
        return holderFragmentFor(fragment.getChildFragmentManager());
    }

    private static ListenActivityResultFragment holderFragmentFor(FragmentManager fm) {
        ListenActivityResultFragment holder = findHolderFragment(fm);
        if (holder == null) {
            holder = createHolderFragment(fm);
        }
        return holder;
    }

    private static ListenActivityResultFragment createHolderFragment(FragmentManager fragmentManager) {
        ListenActivityResultFragment holder = new ListenActivityResultFragment();
        fragmentManager.beginTransaction().add(holder, HOLDER_TAG).commitNowAllowingStateLoss(); // need quickly
        return holder;
    }

    private static ListenActivityResultFragment findHolderFragment(FragmentManager manager) {
        if (manager.isDestroyed()) {
            throw new IllegalStateException("Can't access FragmentManager from onDestroy");
        }

        Fragment fragmentByTag = manager.findFragmentByTag(HOLDER_TAG);
        if (fragmentByTag != null && !(fragmentByTag instanceof ListenActivityResultFragment)) {
            throw new IllegalStateException("Unexpected fragment instance was returned by HOLDER_TAG");
        }
        return (ListenActivityResultFragment) fragmentByTag;
    }
}
