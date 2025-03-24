package com.rhythmcoderzzf.ez.utils.phone;

import android.Manifest;
import android.content.Context;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresPermission;

public class EZPhoneUtil {
    private final Context context;
    private final TelephonyManager telephonyManager;

    public EZPhoneUtil(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @RequiresPermission(anyOf = {Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE})
    public String getPhoneNumber() {
        return telephonyManager.getLine1Number();
    }


}
