package com.androiddeveloper.chat.utils;

import android.content.Context;

/**
 * 文件路径工具类
 */
public class FilePathUtil {
    public static String ROOT_PATH = ContextUtil.applicationContext.getFilesDir().getPath()
            + "/" + Constants.APPLICATION_NAME;

    /**
     * apk下载文件夹
     */
    public static String getApkDownloadFolder(Context context) {
        //    /data/user/0/com.androiddeveloper.chat/cache/apk/2-1.0.1.apk
        return context.getCacheDir().getPath() + "/" + Constants.APPLICATION_NAME + "/apk";
    }

    /**
     * 录音频文件夹
     *
     * @return
     */
    public static String getAudioFolder() {
        return ROOT_PATH + "/" + UserUtil.userId + "/audio";
    }

    /**
     * 图片文件夹
     *
     * @return
     */
    public static String getImageFolder() {
        return ROOT_PATH + "/" + UserUtil.userId + "/image";
    }
}
