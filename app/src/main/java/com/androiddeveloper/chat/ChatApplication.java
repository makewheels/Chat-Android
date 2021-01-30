package com.androiddeveloper.chat;

import android.app.Application;

import com.androiddeveloper.chat.utils.ContextUtil;

import cn.jpush.android.api.JPushInterface;

public class ChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ContextUtil.applicationContext = getApplicationContext();

//        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

    }

}
