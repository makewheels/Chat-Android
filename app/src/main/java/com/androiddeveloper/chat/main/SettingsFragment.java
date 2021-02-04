package com.androiddeveloper.chat.main;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class SettingsFragment extends BaseFragment {

    private TextView textView;

    @Override
    protected View initView() {
        textView = new TextView(context);
        textView.setText(SettingsFragment.class.getName());
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

}
