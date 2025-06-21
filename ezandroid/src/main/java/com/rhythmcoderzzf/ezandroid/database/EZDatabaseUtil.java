package com.zzf.ezandroid.database;

import android.content.Context;
import android.content.SharedPreferences;

public class EZDatabaseUtil {
    private Context mContext;
    private SharedPreferenceModule sharedPreferenceModule;

    public EZDatabaseUtil(Context context) {
        mContext = context;
    }

    public SharedPreferenceModule getSharedPreferenceModule(String preferenceFileName) {
        if (sharedPreferenceModule == null) {
            sharedPreferenceModule = new SharedPreferenceModule(preferenceFileName);
        }
        return sharedPreferenceModule;
    }

    public class SharedPreferenceModule {
        private String preferenceFileName;

        public SharedPreferenceModule(String preferenceFileName) {
            this.preferenceFileName = preferenceFileName;
        }

        public void setValue(String key, String value) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }

        public String getValue(String key) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
            return sharedPreferences.getString(key, "");
        }

    }

}
