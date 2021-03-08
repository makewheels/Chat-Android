package com.androiddeveloper.chat.oss;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;

/**
 * 上传对象存储工具类
 */
public class UploadUtil {

    public static TransferManager getTransferManager(
            Context context, String region, CredentialProvider credentialProvider) {
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region).isHttps(true).builder();
        CosXmlService cosXmlService = new CosXmlService(context,
                serviceConfig, credentialProvider);
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        return new TransferManager(cosXmlService, transferConfig);
    }
}
