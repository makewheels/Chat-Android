package com.androiddeveloper.chat.utils;

import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;
import lombok.Data;
import okhttp3.Call;

/**
 * 阿里云对象存储工具类
 */
public class OssUtil {
    @Data
    static class Credentials {
        private String securityToken;
        private String accessKeySecret;
        private String accessKeyId;
        private Date expiration;
    }

    private static Credentials credentials;

    /**
     * 更新凭证
     */
    private static void updateCredentials() {
        //先看过没过期
        Date currentDate = new Date();
        //如果没过期就不更新了
        if (!currentDate.after(credentials.getExpiration()))
            return;
        HttpUtil.post("/oss/getStsCredential", null, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(ContextUtil.applicationContext,
                        "/oss/getStsCredential " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<Credentials> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<Credentials>>(Result.class) {
                        });
                if (result.getCode() != Code.SUCCESS)
                    return;
                credentials = result.getData();
            }
        });
    }

    private static final OSS oss;

    static {
        String endpoint = "https://oss-cn-beijing.aliyuncs.com";
        String stsServer = "STS应用服务器地址，例如http://abc.com";
        OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider(stsServer);
        oss = new OSSClient(ContextUtil.applicationContext, endpoint, credentialProvider);
    }

    /**
     * 上传
     *
     * @param uploadFile
     * @param objectKey
     */
    public static void upload(File uploadFile, String objectKey) {
        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest(
                "chat-oss-bucket", objectKey, uploadFile.getPath());
        put.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", "http://abc.com/callback.php");
                put("callbackBody", "filename=${object}&size=${size}&photo=${x:photo}&system=${x:system}");
            }
        });
        put.setCallbackVars(new HashMap<String, String>() {
            {
                put("x:phone", "IPOHE6S");
                put("x:system", "YunOS5.0");
            }
        });
        try {
            PutObjectResult putResult = oss.putObject(put);
            Log.e("PutObject", "UploadSuccess");
            Log.e("ETag", putResult.getETag());
            Log.e("RequestId", putResult.getRequestId());
        } catch (ClientException e) {
            // 本地异常，如网络异常等。
            e.printStackTrace();
        } catch (ServiceException e) {
            // 服务异常。
            Log.e("RequestId", e.getRequestId());
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        }
    }

}
