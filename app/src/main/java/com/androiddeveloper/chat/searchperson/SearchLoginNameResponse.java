package com.androiddeveloper.chat.searchperson;

import lombok.Data;

@Data
public class SearchLoginNameResponse {
    private String userId;
    private String loginName;
    private String nickName;
    private String headImageUrl;
}
