package com.androiddeveloper.chat.oss;

import java.util.Date;

import lombok.Data;

/**
 * @Author makewheels
 * @Time 2021.03.08 22:17:46
 */
@Data
public class OssCredential {
    private Credentials credentials;
    private Date expiration;
    private long startTime;
    private long expiredTime;
}

@Data
class Credentials {
    private String tmpSecretId;
    private String tmpSecretKey;
    private String sessionToken;
}
