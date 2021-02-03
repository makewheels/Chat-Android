package com.androiddeveloper.chat.main;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class MessageFragment extends BaseFragment {

    private TextView textView;

    @Override
    protected View initView() {
        textView = new TextView(context);
        textView.setText(MessageFragment.class.getName());
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    @Override
    protected void initData() {
        super.initData();
    }
}
