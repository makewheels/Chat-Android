package com.androiddeveloper.chat.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {
    private EditText et_loginName;
    private EditText et_password;
    private Button btn_login;
    private Button btn_to_register;

    private static final int REQUEST_CODE_START_LOGIN_ACTIVITY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //先看有没有loginToken
        String loginToken = LoginTokenUtil.getLoginToken();
        //如果有loginToken，跳转主页面
        if (StringUtils.isNotEmpty(loginToken)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        initView();
        setClickListener();
    }

    private void initView() {
        et_loginName = findViewById(R.id.et_loginName);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_to_register = findViewById(R.id.btn_to_register);
    }

    private void setClickListener() {
        btn_login.setOnClickListener(v -> click_btn_login());
        btn_to_register.setOnClickListener(v -> {
            //前往注册页面的时候，要一个结果
            //一种情况是用户直接返回，不需要任何操作
            //还有一种情况是用户在注册页注册成功，那就要finish掉登录Activity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REQUEST_CODE_START_LOGIN_ACTIVITY);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_LOGIN_ACTIVITY) {
            if (resultCode == 1) {
                finish();
            }
        }
    }

    /**
     * 登录按钮
     */
    private void click_btn_login() {
        btn_login.setEnabled(false);
        String loginName = et_loginName.getText().toString();
        String password = et_password.getText().toString();
        String jpushRegistrationId = JPushInterface.getRegistrationID(this);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("loginName", loginName);
        paramsMap.put("password", password);
        paramsMap.put("jpushRegistrationId", jpushRegistrationId);
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
                    SharedPreferencesUtil.putString(Constants.KEY_HEAD_IMAGE_URL, userInfoResponse.getHeadImageUrl());
                }
                Toasty.success(LoginActivity.this, R.string.login_success).show();
                // 跳转到主页面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("data", response);
                startActivity(intent);
                finish();
            }
        });
    }
}