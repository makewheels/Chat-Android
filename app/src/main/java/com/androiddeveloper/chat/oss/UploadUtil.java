package com.androiddeveloper.chat.oss;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;

/**
 * 上传对象存储工具类
 */
public class UploadUtil {
    public static class SessionCredentialProvider extends BasicLifecycleCredentialProvider {
        @Override
        protected QCloudLifecycleCredentials fetchNewCredentials() {
            // 首先从您的临时密钥服务器获取包含了密钥信息的响应

            // 然后解析响应，获取临时密钥信息
            String tmpSecretId = "COS_SECRETID"; // 临时密钥 SecretId
            String tmpSecretKey = "COS_SECRETKEY"; // 临时密钥 SecretKey
            String sessionToken = "COS_SESSIONTOKEN"; // 临时密钥 Token
            long expiredTime = 1556183496L;//临时密钥有效截止时间戳，单位是秒
            //建议返回服务器时间作为签名的开始时间，避免由于用户手机本地时间偏差过大导致请求过期
            // 返回服务器时间作为签名的起始时间
            long startTime = 1556182000L; //临时密钥有效起始时间，单位是秒
            return new SessionQCloudCredentials(tmpSecretId, tmpSecretKey,
                    sessionToken, startTime, expiredTime);
        }
    }

    public TransferManager getTransferManager(Context context, String region) {
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region).isHttps(true).builder();
        CosXmlService cosXmlService = new CosXmlService(context,
                serviceConfig, new SessionCredentialProvider());
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        return new TransferManager(cosXmlService, transferConfig);
    }
}
