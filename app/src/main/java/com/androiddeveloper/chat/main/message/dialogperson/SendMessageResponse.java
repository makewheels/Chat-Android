package com.androiddeveloper.chat.main.message.dialogperson;

import com.androiddeveloper.chat.oss.OssCredential;

import java.util.Date;

import lombok.Data;

/**
 * @Author makewheels
 * @Time 2021.01.31 16:05:16
 */
@Data
public class SendMessageResponse {
    private String messageId;
    private String conversationId;
    private String fromUserId;
    private String toUserId;
    private Date createTime;

    //以下只有在上传文件的时候才调用
    private String region;
    private String bucket;
    private String object;

    private Boolean isNeedUpload;

    private OssCredential ossCredential;
}
