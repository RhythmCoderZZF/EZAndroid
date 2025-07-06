package com.rhythmcoderzzf.baselib;

import android.app.Activity;

/**
 * Author:create by RhythmCoderZZF
 * Date:2023/12/16
 * Description:
 */
public class TitleBean {
    private String mTitle;
    private String mSubTitle;
    private String mInfo;
    private Class<? extends Activity> mActivityClass;

    public TitleBean(String title, Class<? extends Activity> clazz) {
        this(title, "", "", clazz);
    }

    public TitleBean(String title, String subTitle, String info, Class<? extends Activity> clazz) {
        mTitle = title;
        mSubTitle = subTitle;
        mInfo = info;
        mActivityClass = clazz;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String mSubTitle) {
        this.mSubTitle = mSubTitle;
    }

    public Class<? extends Activity> getActivityClass() {
        return mActivityClass;
    }

    public void setActivityClass(Class<Activity> mActivityClass) {
        this.mActivityClass = mActivityClass;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String mInfo) {
        this.mInfo = mInfo;
    }

    @Override
    public String toString() {
        return "TitleBean{" + "mTitle='" + mTitle + '\'' + ", mSubTitle='" + mSubTitle + '\'' + ", mInfo='" + mInfo + '\'' + ", mActivityClass=" + mActivityClass + '}';
    }
}
