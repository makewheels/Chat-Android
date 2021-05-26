package com.androiddeveloper.chat.utils;

import android.content.Context;

import com.androiddeveloper.chat.main.message.dialogperson.PersonMessage;

import java.io.File;

/**
 * 文件路径工具类
 */
public class FilePathUtil {
    //    /data/user/0/com.androiddeveloper.chat/files/chat
    public static String ROOT_PATH = ContextUtil.applicationContext.getFilesDir().getPath()
            + "/" + Constants.APPLICATION_NAME;

    public static String getRootPath() {
        return ROOT_PATH;
    }

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
    public static File getAudioFolder() {
        //    /data/user/0/com.androiddeveloper.chat/files/chat
        //    /user60ed442772984f43820a559101371a93/audio
        File file = new File(ROOT_PATH + "/" + UserUtil.userId + "/audio");
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    /**
     * 获取文件路径
     *
     * @param personMessage
     * @return
     */
    public static File getFile(PersonMessage personMessage) {
        return new File(FilePathUtil.getRootPath() + "/"
                + personMessage.getConversationId() + "/"
                + personMessage.getMessageType() + "/" + personMessage.getFileName());
    }

}
