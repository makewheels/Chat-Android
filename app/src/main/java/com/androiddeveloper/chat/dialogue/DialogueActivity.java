package com.androiddeveloper.chat.dialogue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.main.Conversation;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class DialogueActivity extends AppCompatActivity {
    private Conversation conversation;

    private TextView tv_nickname;
    private EditText et_input;
    private Button btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);

        initViews();

        Intent intent = getIntent();
        conversation = JSON.parseObject(
                intent.getStringExtra("Conversation"), Conversation.class);
        tv_nickname.setText(conversation.getTitle());

        addListeners();
    }

    private void initViews() {
        tv_nickname = findViewById(R.id.tv_nickname);
        btn_send = findViewById(R.id.btn_send);
        et_input = findViewById(R.id.et_input);
    }

    private void addListeners() {
        btn_send.setOnClickListener(v -> {
            String input = et_input.getText().toString();
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("conversationId", conversation.getConversationId());
            paramsMap.put("messageType", "text");
            paramsMap.put("content", input);
            HttpUtil.post("/message/person/sendMessage", paramsMap, new CallBackUtil.CallBackString() {
                @Override
                public void onFailure(Call call, Exception e) {
                    Toasty.error(DialogueActivity.this,
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
                        Toasty.error(DialogueActivity.this, result.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            });
        });
    }
}