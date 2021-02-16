package com.androiddeveloper.chat.main;

import lombok.Data;

@Data
public class LatestInfoResponse {
    private Integer versionCode;
    private String versionName;
    private String description;
    private Boolean isForceUpdate;
    private String apkDownloadUrl;
}
