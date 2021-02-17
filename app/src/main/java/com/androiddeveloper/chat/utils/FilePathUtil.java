package com.androiddeveloper.chat.utils;

import android.content.Context;

public class FilePathUtil {
    /**
     * apk下载文件夹
     *
     * @param context
     * @return
     */
    public static String getApkDownloadFolder(Context context) {
        return context.getCacheDir().getPath() + "/apk";
    }
}
