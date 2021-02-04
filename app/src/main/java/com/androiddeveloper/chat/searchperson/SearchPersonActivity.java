package com.androiddeveloper.chat.searchperson;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class SearchPersonActivity extends AppCompatActivity {
    private EditText et_loginName;
    private Button btn_search;
    private ImageView iv_head;
    private TextView tv_searchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_person);

        initView();
        addListeners();
    }

    private void initView() {
        et_loginName = findViewById(R.id.et_loginName);
        btn_search = findViewById(R.id.btn_search);
        iv_head = findViewById(R.id.iv_head);
        tv_searchResult = findViewById(R.id.tv_searchResult);
    }

    private void addListeners() {
        btn_search.setOnClickListener(v -> {
            String q = et_loginName.getText().toString();
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("loginName", q);
            HttpUtil.post("/user/searchUserByLoginName", paramsMap, new CallBackUtil.CallBackString() {
                @Override
                public void onFailure(Call call, Exception e) {
                    Toasty.error(SearchPersonActivity.this,
                            "searchUserByLoginName onFailure " + R.string.error_occurred_please_retry,
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                @Override
                public void onResponse(String response) {
                    Result<SearchLoginNameResponse> result
                            = JSON.parseObject(response,
                            new TypeReference<Result<SearchLoginNameResponse>>(Result.class) {
                            });
                    Log.e("tag", response);

                    int code = result.getCode();
                    //如果是错误码
                    if (code != 0) {
                        tv_searchResult.setTextColor(Color.RED);
                        tv_searchResult.setText(result.getMessage());
                        return;
                    }
                    //如果正常搜索到了用户
                    tv_searchResult.setTextColor(Color.BLACK);
                    tv_searchResult.setText(result.getData().getLoginName());
                }
            });
        });
    }
}