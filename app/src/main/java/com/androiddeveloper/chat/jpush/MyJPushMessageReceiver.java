package com.androiddeveloper.chat.jpush;

import android.content.Context;
import android.util.Log;

import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class MyJPushMessageReceiver extends JPushMessageReceiver {
    private final JpushHandler jpushHandler = new JpushHandler();

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        String message = customMessage.message;
        Log.e("tag", "MyJPushMessageReceiver onMessage " + message);
        jpushHandler.handleMessage(message);
    }

    @Override
    public void onRegister(Context context, String s) {
        String registrationID = JPushInterface.getRegistrationID(context);
        Log.e("tag", "MyJPushMessageReceiver onRegister-registrationID " + registrationID);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        Log.e("getErrorCode", jPushMessage.getErrorCode() + "");
        Log.e("getAlias", jPushMessage.getAlias());
        Log.e("getTags", jPushMessage.getTags() + "");
    }

}
