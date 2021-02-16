package com.androiddeveloper.chat.common;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.androiddeveloper.chat.utils.ContextUtil;
import com.tencent.bugly.crashreport.CrashReport;

import cn.jpush.android.api.JPushInterface;

public class ChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ContextUtil.applicationContext = getApplicationContext();

        CrashReport.initCrashReport(getApplicationContext(), "ee6576552a", true);

//        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
