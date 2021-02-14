package com.androiddeveloper.chat.main.settings;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.androiddeveloper.chat.main.BaseFragment;

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
