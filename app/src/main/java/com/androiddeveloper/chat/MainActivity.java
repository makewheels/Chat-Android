package com.androiddeveloper.chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androiddeveloper.chat.utils.CallBackUtil;
import com.androiddeveloper.chat.utils.OkhttpUtil;

import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String registrationID = JPushInterface.getRegistrationID(this);
        Log.e("tag", "MainActivity-registrationID " + registrationID);

        String url = "http://10.4.137.239:8080/sendMessage?message=haelfgwjieof234f41";
        OkhttpUtil.get(url, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                Log.e("tag", response);
            }
        });
    }
}