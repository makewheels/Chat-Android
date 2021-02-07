package com.androiddeveloper.chat.jpush;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class JpushHandler {
    public void handleMessage(String message) {
        Map<String, String> map = new HashMap<>();
        String[] split = message.split("&");
        for (String each : split) {
            String[] kv = each.split("=");
            String key = kv[0];
            String value = kv[1];
            map.put(key, value);
        }
        String messageId = map.get("messageId");
        pullMessage(messageId);
    }

    public void pullMessage(String messageId) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("messageId", messageId);
        HttpUtil.post("/message/person/pullByMessageId", paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<PullMessageResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<PullMessageResponse>>(Result.class) {
                        });
                Log.e("tag", response);
                Log.e("tag", result.getData().getContent());
            }
        });
    }
}
