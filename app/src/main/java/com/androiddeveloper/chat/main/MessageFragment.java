package com.androiddeveloper.chat.main;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.searchperson.SearchPersonActivity;

public class MessageFragment extends BaseFragment {

    private Button btn_search_person;
    private static final int REQUEST_CODE_SEARCH_PERSON = 0;

    @Override
    protected View initView() {
        View view = View.inflate(context, R.layout.fragment_message, null);
        btn_search_person = view.findViewById(R.id.btn_search_person);
        btn_search_person.setOnClickListener(v -> {
            Intent intent = new Intent(context, SearchPersonActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SEARCH_PERSON);
        });
        return view;
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SEARCH_PERSON){

        }
    }

}
