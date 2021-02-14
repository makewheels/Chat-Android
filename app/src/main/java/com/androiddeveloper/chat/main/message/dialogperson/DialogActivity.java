package com.androiddeveloper.chat.main.message.dialogperson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.jpush.PullMessageResponse;
import com.androiddeveloper.chat.main.message.conversation.Conversation;
import com.androiddeveloper.chat.utils.MessageType;
import com.androiddeveloper.chat.utils.MyInfoUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class DialogActivity extends AppCompatActivity {
    private Conversation conversation;

    private TextView tv_nickname;
    private RecyclerView rv_dialog;
    private EditText et_input;
    private Button btn_send;

    private MessageAdapter messageAdapter;

    public MessageReceiver messageReceiver;
    public static final String ACTION_RECEIVE_PERSON_MESSAGE = "com.gc.broadcast.receiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);

        initViews();

        Intent intent = getIntent();
        String conversationJson = intent.getStringExtra("Conversation");
        conversation = JSON.parseObject(conversationJson, Conversation.class);
        tv_nickname.setText(conversation.getTitle());

        addListeners();
    }

    private void initViews() {
        tv_nickname = findViewById(R.id.tv_nickname);
        rv_dialog = findViewById(R.id.rv_dialog);
        btn_send = findViewById(R.id.btn_send);
        et_input = findViewById(R.id.et_input);

        messageAdapter = new MessageAdapter(this, new ArrayList<>());
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv_dialog.setLayoutManager(linearLayoutManager);
        rv_dialog.setAdapter(messageAdapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVE_PERSON_MESSAGE);
        registerReceiver(new MessageReceiver(), filter);
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_RECEIVE_PERSON_MESSAGE)) {
                String messageId = intent.getStringExtra("messageId");
                pullPersonMessage(messageId);
            }
        }
    }

    private void addListeners() {
        //发送消息按钮
        btn_send.setOnClickListener(v -> {
            String input = et_input.getText().toString();
            //检查输入内容是否为空
            if (StringUtils.isEmpty(input)) {
                Toasty.info(DialogActivity.this, R.string.please_input_content,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("conversationId", conversation.getConversationId());
            paramsMap.put("messageType", MessageType.TEXT);
            paramsMap.put("content", input);
            HttpUtil.post("/message/person/sendMessage", paramsMap, new CallBackUtil.CallBackString() {
                @Override
                public void onFailure(Call call, Exception e) {
                    Toasty.error(DialogActivity.this,
                            "person sendMessage onFailure " + R.string.error_occurred_please_retry,
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                @Override
                public void onResponse(String response) {
                    Result<SendMessageResponse> result
                            = JSON.parseObject(response,
                            new TypeReference<Result<SendMessageResponse>>(Result.class) {
                            });
                    if (result.getCode() != Code.SUCCESS) {
                        Toasty.error(DialogActivity.this, result.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //到这里，说明是发送成功了
                    //清空输入框
                    et_input.setText("");
                    //更新recycle view
                    SendMessageResponse sendMessageResponse = result.getData();
                    PersonMessage personMessage = new PersonMessage();
                    personMessage.setMessageId(sendMessageResponse.getMessageId());
                    personMessage.setConversationId(sendMessageResponse.getConversationId());
                    personMessage.setFromUserId(sendMessageResponse.getFromUserId());
                    personMessage.setToUserId(sendMessageResponse.getToUserId());
                    personMessage.setSenderHeadUrl(MyInfoUtil.headImageUrl);
                    personMessage.setIsSend(true);
                    personMessage.setMessageType(MessageType.TEXT);
                    personMessage.setContent(input);
                    personMessage.setCreateTime(sendMessageResponse.getCreateTime());
                    addMessage(personMessage);
                }
            });
        });
    }

    public void addMessage(PersonMessage personMessage) {
        //添加到dialog里
        messageAdapter.addMessage(personMessage);
        //滑动到最低端
        rv_dialog.scrollToPosition(messageAdapter.getItemCount() - 1);
        //写入数据库
    }

    /**
     * 拉取对个人发的消息
     *
     * @param messageId
     */
    public void pullPersonMessage(String messageId) {
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
                PullMessageResponse data = result.getData();
                PersonMessage personMessage = new PersonMessage();
                personMessage.setMessageId(data.getMessageId());
                personMessage.setConversationId(data.getConversationId());
                personMessage.setFromUserId(data.getFromUserId());
                personMessage.setToUserId(data.getToUserId());
                personMessage.setSenderHeadUrl(conversation.getHeadImageUrl());
                personMessage.setIsSend(false);
                personMessage.setMessageType(MessageType.TEXT);
                personMessage.setContent(data.getContent());
                personMessage.setCreateTime(data.getCreateTime());
                addMessage(personMessage);
            }
        });
    }
}