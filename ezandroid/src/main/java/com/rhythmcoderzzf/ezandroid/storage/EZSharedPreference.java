package com.rhythmcoderzzf.ezandroid.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.rhythmcoderzzf.ezandroid.core.AbstractBuilder;

/**
 * EZ SharedPreference持久化。使用方式：
 * <pre>
 *    EZSharedPreference ezSharePreference = new EZSharedPreference.Builder(this)
 *                 .applyApplyMode(true)
 *                 .applyPreferenceFileName(sharedPreferenceFileName)
 *                 .build();
 *
 *    ezSharePreference.putString("hello", "Jack");
 *    eezSharePreference.getString("hello", "");
 * </pre>
 */
public class EZSharedPreference {
    private String mPreferenceFileName;
    private boolean mApplyMode;
    private final SharedPreferences mSharedPreferences;
    private final SharedPreferences.Editor mEditor;

    private EZSharedPreference(Context context) {
        mSharedPreferences = context.getSharedPreferences(mPreferenceFileName, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void putString(String key, String value) {
        mEditor.putString(key, value);
        if (mApplyMode) mEditor.apply();
        else mEditor.commit();
    }

    public void putInt(String key, int value) {
        mEditor.putInt(key, value);
        if (mApplyMode) mEditor.apply();
        else mEditor.commit();
    }

    public void putBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        if (mApplyMode) mEditor.apply();
        else mEditor.commit();
    }

    public String getString(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }

    public static class Builder implements AbstractBuilder<EZSharedPreference> {
        private Context tmpContext;
        private String tmpPreferenceFileName = "EZSharedPreference";
        private boolean tmpApplyMode = true;//默认apply（异步）模式

        public Builder(Context context) {
            tmpContext = context;
        }

        public Builder applyApplyMode(boolean applyMode) {
            tmpApplyMode = applyMode;
            return this;
        }

        public Builder applyPreferenceFileName(String preferenceFileName) {
            tmpPreferenceFileName = preferenceFileName;
            return this;
        }

        @Override
        public EZSharedPreference build() {
            EZSharedPreference preference = new EZSharedPreference(tmpContext);
            preference.mApplyMode = tmpApplyMode;
            preference.mPreferenceFileName = tmpPreferenceFileName;
            return preference;
        }
    }

}
