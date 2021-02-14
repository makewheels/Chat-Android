package com.androiddeveloper.chat.login;

import java.util.Date;

import lombok.Data;

/**
 * @Author makewheels
 * @Time 2021.01.30 16:56:09
 */
@Data
public class UserInfoResponse {
    private String userId;
    private String loginName;
    private String nickname;
    private String headImageUrl;
    private String loginToken;
    private String phone;
    private String jpushRegistrationId;
    private Date createTime;
}
