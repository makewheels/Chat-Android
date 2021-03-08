package com.androiddeveloper.chat.oss;

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;

public class CredentialProvider extends BasicLifecycleCredentialProvider {
    private OssCredential ossCredential;

    private CredentialProvider() {
    }

    public CredentialProvider(OssCredential ossCredential) {
        this.ossCredential = ossCredential;
    }

    @Override
    protected QCloudLifecycleCredentials fetchNewCredentials() {
        Credentials credentials = ossCredential.getCredentials();
        String tmpSecretId = credentials.getTmpSecretId();
        String tmpSecretKey = credentials.getTmpSecretKey();
        String sessionToken = credentials.getSessionToken();
        long startTime = ossCredential.getStartTime();
        long expiredTime = ossCredential.getExpiredTime();
        return new SessionQCloudCredentials(tmpSecretId, tmpSecretKey,
                sessionToken, startTime, expiredTime);
    }
}
