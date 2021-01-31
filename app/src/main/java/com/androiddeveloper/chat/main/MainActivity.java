package com.androiddeveloper.chat.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.login.LoginActivity;
import com.androiddeveloper.chat.utils.LoginTokenUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    private TextView tv_hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_hello = findViewById(R.id.tv_hello);

        Intent intent = getIntent();
        String hello = intent.getStringExtra("data");
        tv_hello.setText(hello + " this is main");

        checkLoginToken();
    }

    private void checkLoginToken() {
        HttpUtil.post("/user/checkLoginToken", null, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(MainActivity.this,
                        "checkLoginToken onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Log.e("tag", response);
                Result<Void> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<Void>>(Result.class) {
                        });
                //如果需要登录，跳转到登录Activity
                if (result.getCode() == Code.CHECK_LOGIN_TOKEN_ERROR) {
                    Toasty.error(MainActivity.this,
                            "checkLoginToken onResponse " + result.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    LoginTokenUtil.removeLoginToken();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }
}