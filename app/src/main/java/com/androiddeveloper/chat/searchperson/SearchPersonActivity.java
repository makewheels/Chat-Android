package com.androiddeveloper.chat.searchperson;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;
import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class SearchPersonActivity extends AppCompatActivity {
    private TextView tv_hint;
    private EditText et_loginName;
    private Button btn_search;
    private ImageView iv_head;
    private TextView tv_searchResult;
    private Button btn_start_conversation;

    private String targetUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_person);

        initView();
        addListeners();
    }

    private void initView() {
        tv_hint = findViewById(R.id.tv_hint);
        et_loginName = findViewById(R.id.et_loginName);
        btn_search = findViewById(R.id.btn_search);
        iv_head = findViewById(R.id.iv_head);
        tv_searchResult = findViewById(R.id.tv_searchResult);
        btn_start_conversation = findViewById(R.id.btn_start_conversation);
    }

    private void addListeners() {
        //搜索按钮
        btn_search.setOnClickListener(v -> {
            String q = et_loginName.getText().toString();
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("loginName", q);
            HttpUtil.post("/user/searchUserByLoginName", paramsMap, new CallBackUtil.CallBackString() {
                @Override
                public void onFailure(Call call, Exception e) {
                    Toasty.error(SearchPersonActivity.this,
                            "searchUserByLoginName onFailure" + R.string.error_occurred_please_retry,
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                @Override
                public void onResponse(String response) {
                    Result<SearchLoginNameResponse> result
                            = JSON.parseObject(response,
                            new TypeReference<Result<SearchLoginNameResponse>>(Result.class) {
                            });
                    //如果是错误码
                    if (result.getCode() != Code.SUCCESS) {
                        tv_hint.setText(R.string.please_input_person_login_name);
                        iv_head.setVisibility(View.GONE);
                        tv_searchResult.setTextColor(Color.RED);
                        tv_searchResult.setText(result.getMessage());
                        btn_start_conversation.setEnabled(false);
                        return;
                    }
                    //如果正常搜索到了用户，显示用户名和图标
                    tv_hint.setText(R.string.choose_to_start_conversation);
                    SearchLoginNameResponse data = result.getData();
                    tv_searchResult.setTextColor(Color.BLACK);
                    String text = data.getLoginName() + " " + data.getNickName();
                    tv_searchResult.setText(text);
                    iv_head.setVisibility(View.VISIBLE);
                    Log.e("tag", data.getHeadImageUrl());
                    Glide.with(SearchPersonActivity.this)
                            .load(data.getHeadImageUrl())
                            .placeholder(R.drawable.default_placeholder)
                            .into(iv_head);
                    targetUserId = data.getUserId();
                    btn_start_conversation.setEnabled(true);
                }
            });
        });
        //发起会话按钮
        btn_start_conversation.setOnClickListener(v -> {
            if (StringUtils.isEmpty(targetUserId)) {
                Toasty.error(SearchPersonActivity.this, R.string.error_occurred_please_retry,
                        Toasty.LENGTH_SHORT).show();
                return;
            }
            //发起请求，创建会话
            btn_start_conversation.setEnabled(false);
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("type", "person");
            paramsMap.put("targetId", targetUserId);
            HttpUtil.post("/conversation/create", paramsMap, new CallBackUtil.CallBackString() {
                @Override
                public void onFailure(Call call, Exception e) {
                    btn_start_conversation.setEnabled(true);
                    Toasty.error(SearchPersonActivity.this,
                            "searchUserByLoginName onFailure " + R.string.error_occurred_please_retry,
                            Toasty.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                @Override
                public void onResponse(String response) {
                    btn_start_conversation.setEnabled(true);
                    //先看服务器给的数据是什么
                    Result<ConversationResponse> result
                            = JSON.parseObject(response,
                            new TypeReference<Result<ConversationResponse>>(Result.class) {
                            });
                    //创建会话失败
                    if (result.getCode() != Code.SUCCESS) {
                        Toasty.error(SearchPersonActivity.this,
                                result.getMessage(), Toasty.LENGTH_SHORT).show();
                        return;
                    }
                    //创建会话成功
                    Toasty.success(SearchPersonActivity.this,
                            R.string.create_conversation_success, Toasty.LENGTH_SHORT).show();
                    //其实这里应该写入数据库
                    Intent intent = getIntent();
                    intent.putExtra("ConversationResponse", JSON.toJSONString(result.getData()));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        });
    }
}