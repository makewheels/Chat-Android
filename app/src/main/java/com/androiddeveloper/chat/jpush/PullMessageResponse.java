package com.androiddeveloper.chat.jpush;

import java.util.Date;

import lombok.Data;

/**
 * @Author makewheels
 * @Time 2021.01.31 23:53:44
 */
@Data
public class PullMessageResponse {
    private String messageId;

    private String conversationId;
    private String messageType;

    private String fromUserId;

    private String toUserId;

    private Boolean isArrive;

    private Date arriveTime;

    private Boolean isRead;

    private Date readTime;

    private Date createTime;

    private String content;
}
