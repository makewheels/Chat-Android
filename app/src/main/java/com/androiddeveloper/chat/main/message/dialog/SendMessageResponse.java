package com.androiddeveloper.chat.main.message.dialog;

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

}
