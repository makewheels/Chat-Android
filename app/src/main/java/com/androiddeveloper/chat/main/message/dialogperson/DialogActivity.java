package com.androiddeveloper.chat.main.message.dialogperson;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.androiddeveloper.chat.utils.UserUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;
import com.permissionx.guolindev.PermissionX;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class DialogActivity extends AppCompatActivity {
    private static final int RC_CHOOSE_PHOTO = 0;

    private Conversation conversation;

    private TextView tv_nickname;
    private RecyclerView rv_dialog;

    private Button btn_audio;
    private Button btn_image;

    private EditText et_input;
    private Button btn_send;

    private MessageAdapter messageAdapter;

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
        btn_audio = findViewById(R.id.btn_audio);
        btn_image = findViewById(R.id.btn_image);

        messageAdapter = new MessageAdapter(this, new ArrayList<>());
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv_dialog.setLayoutManager(linearLayoutManager);
        rv_dialog.setAdapter(messageAdapter);

        loadButtonLeftIcon(R.drawable.microphone, btn_audio);
        loadButtonLeftIcon(R.drawable.image, btn_image);
        loadButtonLeftIcon(R.drawable.send, btn_send);

        //收到推送消息的，广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVE_PERSON_MESSAGE);
        registerReceiver(new MessageReceiver(), filter);
    }

    //加载图片左边的图标
    private void loadButtonLeftIcon(int id, Button button) {
        Drawable drawable = getResources().getDrawable(id);
        drawable.setBounds(0, 0, 64, 64);
        button.setCompoundDrawables(drawable, null, null, null);
    }

    /**
     * 监听收到推送消息的广播
     */
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //收到人的消息
            if (action.equals(ACTION_RECEIVE_PERSON_MESSAGE)) {
                String messageId = intent.getStringExtra("messageId");
                //拉取人的消息
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
                    personMessage.setSenderHeadUrl(UserUtil.headImageUrl);
                    personMessage.setIsSend(true);
                    personMessage.setMessageType(MessageType.TEXT);
                    personMessage.setContent(input);
                    personMessage.setCreateTime(sendMessageResponse.getCreateTime());
                    addMessage(personMessage);
                }
            });
        });

        //语音按钮
        btn_audio.setOnClickListener(v -> {
            //检查权限
            PermissionX.init(this)
                    .permissions(Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (!allGranted) {
                            Toasty.warning(DialogActivity.this, R.string.permission_denied_record_audio,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        sendAudio();
                    });
        });

        //图片按钮
        btn_image.setOnClickListener(v -> {
            pickImage();
        });
    }

    //检查权限后，发语音
    private void sendAudio() {

    }

    //从相册中选图片
    private void pickImage() {
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
                Toasty.error(DialogActivity.this,
                        "/message/person/pullByMessageId onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
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
                //TODO 这里应该有一个判断，如果打开了这个conversation，那就添加到界面上
                //TODO 写入数据库
                addMessage(personMessage);
                //TODO 其实这里还应该干一件事，上报已达
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == RC_CHOOSE_PHOTO) {
        }

    }

}