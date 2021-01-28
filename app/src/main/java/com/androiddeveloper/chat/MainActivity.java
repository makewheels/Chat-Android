package com.androiddeveloper.chat;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String registrationID = JPushInterface.getRegistrationID(this);
        Log.e("tag", "MainActivity-registrationID " + registrationID);

    }
}