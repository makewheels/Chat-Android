package com.androiddeveloper.chat.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.main.MainActivity;
import com.androiddeveloper.chat.register.RegisterActivity;
import com.androiddeveloper.chat.utils.Constants;
import com.androiddeveloper.chat.utils.LoginTokenUtil;
import com.androiddeveloper.chat.utils.SharedPreferencesUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {
    private EditText et_loginName;
    private EditText et_password;
    private Button btn_login;
    private Button btn_to_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //先看有没有loginToken
        String loginToken = LoginTokenUtil.getLoginToken();
        Log.e("tag", "LoginActivity loginToken " + loginToken + "");
        //如果有loginToken，跳转主页面
        if (loginToken != null && !loginToken.equals("")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        initView();
        setOnClickListener();
    }

    private void initView() {
        et_loginName = findViewById(R.id.et_loginName);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_to_register = findViewById(R.id.btn_to_register);
    }

    private void setOnClickListener() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click_btn_login();
            }
        });
        btn_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    /**
     * 登录按钮
     */
    private void click_btn_login() {
        btn_login.setEnabled(false);
        String loginName = et_loginName.getText().toString();
        String password = et_password.getText().toString();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("loginName", loginName);
        paramsMap.put("password", password);

        HttpUtil.post("/user/login", paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                btn_login.setEnabled(true);
                Toasty.error(LoginActivity.this, R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT, true).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                btn_login.setEnabled(true);
                Result<UserInfoResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<UserInfoResponse>>(Result.class) {
                        });
                int code = result.getCode();
                //如果有错误
                if (code != Code.SUCCESS) {
                    Toasty.error(LoginActivity.this, result.getMessage()).show();
                    return;
                }
                //如果没有错误
                //到这说明已经注册成功了，保存用户信息
                UserInfoResponse userInfoResponse = result.getData();
                if (userInfoResponse != null) {
                    LoginTokenUtil.saveLoginToken(userInfoResponse.getLoginToken());
                    SharedPreferencesUtil.putString(Constants.KEY_USER_ID, userInfoResponse.getUserId());
                    SharedPreferencesUtil.putString(Constants.KEY_LOGIN_NAME, userInfoResponse.getLoginName());
                    SharedPreferencesUtil.putString(Constants.KEY_HEADIMAGEURL, userInfoResponse.getHeadImageUrl());
                }
                // 跳转到主页面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("data", response);
                startActivity(intent);
                finish();
            }
        });
    }
}