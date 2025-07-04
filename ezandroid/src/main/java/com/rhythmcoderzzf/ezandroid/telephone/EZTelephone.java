package com.rhythmcoderzzf.ezandroid.telephone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresPermission;

public class EZTelephone {
    private static final String TAG = EZTelephone.class.getSimpleName();
    private final TelephonyManager telephonyManager;
    private final SubscriptionManager subscriptManager;

    public EZTelephone(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        subscriptManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
    }

    @SuppressLint("HardwareIds")
    @RequiresPermission(anyOf = {Manifest.permission.READ_PHONE_NUMBERS})
    public String getPhoneNumber() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return subscriptManager.getPhoneNumber(telephonyManager.getSubscriptionId());
        }
        return telephonyManager.getLine1Number();
    }
}
