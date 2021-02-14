package com.androiddeveloper.chat.utils;

import java.util.Date;

import lombok.Data;

/**
 * 我的信息
 */
@Data
public class MyInfoUtil {
    public static String userId;

    public static String loginName;

    public static String nickname;

    public static String loginToken;

    public static String jpushRegistrationId;

    public static String headImageUrl;

    public static String phone;

    public static Date createTime;
}
