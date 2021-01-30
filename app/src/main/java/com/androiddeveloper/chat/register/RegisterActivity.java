package com.androiddeveloper.chat.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.main.MainActivity;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

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
        String loginName = et_loginName.getText().toString();
        String password = et_password.getText().toString();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("loginName", loginName);
        paramsMap.put("password", password);

        HttpUtil.post("/user/register", paramsMap,
                new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Toasty.error(RegisterActivity.this, R.string.error_occurred_please_retry,
                                Toast.LENGTH_SHORT, true).show();
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) {
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("hello", response);
                        startActivity(intent);
                    }
                });
    }
}