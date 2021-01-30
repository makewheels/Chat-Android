package com.androiddeveloper.chat.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

    private static final String NAME_DEFAULT = "chat-default";

    private static final SharedPreferences sharedPreferences;

    static {
        sharedPreferences = ContextUtil.applicationContext
                .getSharedPreferences(NAME_DEFAULT, Context.MODE_PRIVATE);
    }

    public static void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public static void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
}
