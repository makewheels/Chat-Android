package com.androiddeveloper.chat.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.login.UserInfoResponse;
import com.androiddeveloper.chat.main.MainActivity;
import com.androiddeveloper.chat.utils.Constants;
import com.androiddeveloper.chat.utils.LoginTokenUtil;
import com.androiddeveloper.chat.utils.SharedPreferencesUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class RegisterActivity extends AppCompatActivity {
    private EditText et_loginName;
    private EditText et_password;
    private Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        setOnClickListener();
    }

    private void initView() {
        et_loginName = findViewById(R.id.et_loginName);
        et_password = findViewById(R.id.et_password);
        btn_register = findViewById(R.id.btn_register);
    }

    private void setOnClickListener() {
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click_btn_register();
            }
        });
    }

    /**
     * 注册按钮
     */
    private void click_btn_register() {
        btn_register.setEnabled(false);
        String loginName = et_loginName.getText().toString();
        String password = et_password.getText().toString();
        String jpushRegistrationId = JPushInterface.getRegistrationID(this);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("loginName", loginName);
        paramsMap.put("password", password);
        paramsMap.put("jpushRegistrationId", jpushRegistrationId);
        HttpUtil.post("/user/register", paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                btn_register.setEnabled(true);
                Toasty.error(RegisterActivity.this, R.string.error_occurred_please_retry,
                        Toasty.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                btn_register.setEnabled(true);
                Result<UserInfoResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<UserInfoResponse>>(Result.class) {
                        });
                int code = result.getCode();
                //如果是错误码，显示错误message
                if (code != Code.SUCCESS) {
                    Toasty.error(RegisterActivity.this, result.getMessage(),
                            Toasty.LENGTH_SHORT).show();
                    return;
                }
                //到这说明已经注册成功了，保存用户信息
                UserInfoResponse userInfoResponse = result.getData();
                if (userInfoResponse != null) {
                    LoginTokenUtil.saveLoginToken(userInfoResponse.getLoginToken());
                    SharedPreferencesUtil.putString(Constants.KEY_USER_ID, userInfoResponse.getUserId());
                    SharedPreferencesUtil.putString(Constants.KEY_LOGIN_NAME, userInfoResponse.getLoginName());
                    SharedPreferencesUtil.putString(Constants.KEY_HEAD_IMAGE_URL, userInfoResponse.getHeadImageUrl());
                }
                //跳转到主界面
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                Toasty.success(RegisterActivity.this, R.string.register_success,
                        Toasty.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}