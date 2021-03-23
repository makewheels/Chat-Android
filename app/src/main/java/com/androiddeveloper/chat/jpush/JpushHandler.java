package com.androiddeveloper.chat.jpush;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.main.message.dialogperson.DialogActivity;
import com.androiddeveloper.chat.utils.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class JpushHandler {
    public void handleMessage(Context context, String data) {
        Map<String, String> map = JSON.parseObject(data,
                new TypeReference<Map<String, String>>(Map.class) {
                });
        String version = map.get("version");
        String cmd = map.get("cmd");
        if (StringUtils.isNotBlank(version))
            return;
        if (version.equals("1")) {
            //拉取消息
            if (cmd.equals("pullMessage")) {
                String type = map.get("type");
                String messageId = map.get("messageId");
                String messageType = map.get("messageType");
                //给人发的消息
                if (type.equals("person")) {
                    //发送广播通知 DialogActivity
                    Intent intent = new Intent(DialogActivity.ACTION_RECEIVE_PERSON_MESSAGE);
                    intent.putExtra("messageId", messageId);
                    intent.putExtra("messageType", messageType);
                    context.sendBroadcast(intent);

                    //群消息
                } else if (type.equals(Constants.CONVERSATION.TYPE_PERSON)) {
                    //TODO 极光推送收到，拉取群消息
                }
            }
        }
    }


}
