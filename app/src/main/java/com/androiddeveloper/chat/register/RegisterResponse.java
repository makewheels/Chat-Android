package com.androiddeveloper.chat.register;

import lombok.Data;

@Data
public class RegisterResponse {
    private String userId;
    private String loginName;
    private String headImageUrl;
    private String loginToken;
}
