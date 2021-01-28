package com.androiddeveloper.chat;

import android.app.Application;
import android.util.Log;

import cn.jpush.android.api.JPushInterface;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

    }
}
