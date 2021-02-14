package com.androiddeveloper.chat.main.message.dialogperson;

import java.util.Date;

import lombok.Data;

/**
 * 发给人的消息，这个bean很通用，用于recycler view
 */
@Data
public class PersonMessage {
    private String messageId;
    private String conversationId;

    private String fromUserId;
    private String toUserId;

    private String senderHeadUrl;//发送者头像

    private Boolean isSend;//是我发送出去的消息，还是我接收到别人发来的消息

    private String messageType;
    private String content;

    private String originalFileName;

    private Boolean isArrive;
    private Date arriveTime;

    private Boolean isRead;
    private Date readTime;

    private Date createTime;
}
