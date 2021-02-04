package com.androiddeveloper.chat.searchperson;

import java.util.Date;

import lombok.Data;

/**
 * @Author makewheels
 * @Time 2021.02.02 23:07:36
 */
@Data
public class ConversationResponse {
    private String conversationId;

    private String userId;

    private String targetId;

    private String type;

    private String title;

    private Integer unreadMessageCount;

    private Date updateTime;
}
