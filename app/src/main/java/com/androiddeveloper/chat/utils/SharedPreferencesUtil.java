package com.androiddeveloper.chat.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

    private static final String NAME_DEFAULT = "chat-default";

    private static final SharedPreferences defaultSharedPreferences;

    static {
        defaultSharedPreferences = ContextUtil.applicationContext
                .getSharedPreferences(NAME_DEFAULT, Context.MODE_PRIVATE);
    }

    public static void putString(String key, String value) {
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key) {
        return defaultSharedPreferences.getString(key, null);
    }

    public static void remove(String key) {
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
}
