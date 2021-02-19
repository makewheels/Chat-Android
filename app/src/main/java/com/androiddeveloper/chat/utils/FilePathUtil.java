package com.androiddeveloper.chat.utils;

import android.content.Context;

/**
 * 文件路径工具类
 */
public class FilePathUtil {
    //    /data/user/0/com.androiddeveloper.chat/files/chat
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
        //    /data/user/0/com.androiddeveloper.chat/files/chat
        //    /user60ed442772984f43820a559101371a93/audio
        return ROOT_PATH + "/" + UserUtil.userId + "/audio";
    }

    /**
     * 图片文件夹
     *
     * @return
     */
    public static String getImageFolder() {
        //    /data/user/0/com.androiddeveloper.chat/files/chat
        //    /user60ed442772984f43820a559101371a93/image
        return ROOT_PATH + "/" + UserUtil.userId + "/image";
    }
}
