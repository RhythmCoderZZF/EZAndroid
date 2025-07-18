package com.rhythmcoderzzf.androidstudysystem.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author:create by RhythmCoderZZF
 * Date:2023/12/9
 * Description:
 * Listen to some USB broadcasts and request usb connection permission.
 */
class USBMonitor {
    private static final String TAG = "USBMonitor";
    private final WeakReference<Context> mWeakContext;
    private PendingIntent mPermissionIntent = null;
    private static final String ACTION_USB_PERMISSION = "com.rhythmcoderzzf.usb.action.USB_PERMISSION";
    private UsbManager mUsbManager;
    private final OnDeviceConnectListener mOnDeviceConnectListener;


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive<< action:" + action);
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (ACTION_USB_PERMISSION.equals(action)) {
                boolean hasPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                Log.d(TAG, "EXTRA_PERMISSION_GRANTED:" + hasPermission);
                if (hasPermission) {
                    if (device != null) {
                        processConnect(device);
                    }
                } else {
                    Toast.makeText(mWeakContext.get(), "DENIED to access USB permission...", Toast.LENGTH_SHORT).show();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                processAttach(device);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                processDettach(device);
            }
        }
    };


    public USBMonitor(UsbManager usbManager, Context context, final OnDeviceConnectListener listener) {
        mWeakContext = new WeakReference<Context>(context);
        mUsbManager = usbManager;
        mOnDeviceConnectListener = listener;
    }


    public void register() {
        Log.d(TAG, "register<<");
        if (mPermissionIntent == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                mPermissionIntent = PendingIntent.getBroadcast(mWeakContext.get(), 123, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
            } else {
                mPermissionIntent = PendingIntent.getBroadcast(mWeakContext.get(), 123, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            }
            final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            mWeakContext.get().registerReceiver(mUsbReceiver, filter);
        }
    }

    public void unRegister() {
        Log.d(TAG, "unRegister<<");
        mWeakContext.get().unregisterReceiver(mUsbReceiver);
        mPermissionIntent = null;
    }

    public void requestPermission(final UsbDevice device) {
        Log.d(TAG, "requestPermission<< device:" + device.getDeviceName());
        if (device != null) {
            if (mUsbManager.hasPermission(device)) {
                Log.d(TAG, "already get permission to access usb device");
                processConnect(device);
            } else {
                try {
                    Log.d(TAG, "need to request permission to access usb device");
                    mUsbManager.requestPermission(device, mPermissionIntent);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<UsbDevice> getDeviceList() {
        final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<>();
        if (deviceList != null) {
            for (final UsbDevice device : deviceList.values()) {
                result.add(device);
            }
        }
        return result;
    }

    private void processConnect(final UsbDevice device) {
        if (mOnDeviceConnectListener != null) {
            mOnDeviceConnectListener.onConnect(device);
        }
    }

    private void processAttach(final UsbDevice device) {
        if (mOnDeviceConnectListener != null) {
            mOnDeviceConnectListener.onAttach(device);
        }
    }

    private void processDettach(final UsbDevice device) {
        if (mOnDeviceConnectListener != null) {
            mOnDeviceConnectListener.onDettach(device);
        }
    }
}
