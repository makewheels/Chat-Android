package com.androiddeveloper.chat.main.message.conversation;

import java.util.Date;

import lombok.Data;

@Data
public class Conversation {
    private String conversationId;

    private String userId;

    private String targetId;

    private String type;

    private String title;
    private String headImageUrl;

    private Integer messageCount;

    private Integer unreadMessageCount;

    private Date updateTime;

    private Date createTime;

}