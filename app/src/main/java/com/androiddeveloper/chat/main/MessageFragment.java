package com.androiddeveloper.chat.main;

import android.view.View;
import android.widget.TextView;

import com.androiddeveloper.chat.R;

public class MessageFragment extends BaseFragment {

    private TextView textView;

    @Override
    protected View initView() {
        View view = View.inflate(context, R.layout.fragment_message, null);
        textView = view.findViewById(R.id.textView);
        textView.setText(System.currentTimeMillis() + "");
        return view;
    }

    @Override
    protected void initData() {
        super.initData();
    }
}
