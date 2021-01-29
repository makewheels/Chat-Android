package com.androiddeveloper.chat.entrance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.register.RegisterActivity;

import cn.jpush.android.api.JPushInterface;

public class EntranceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);

        String registrationID = JPushInterface.getRegistrationID(this);
        Log.e("tag", "MainActivity-registrationID " + registrationID);

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}