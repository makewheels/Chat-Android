package com.androiddeveloper.chat.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.androiddeveloper.chat.R;

public class MainActivity extends AppCompatActivity {
    private TextView tv_hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_hello = findViewById(R.id.tv_hello);

        Intent intent = getIntent();
        String hello = intent.getStringExtra("hello");
        tv_hello.setText(hello);
    }
}