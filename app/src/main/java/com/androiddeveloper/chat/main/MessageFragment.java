package com.androiddeveloper.chat.main;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.searchperson.SearchPersonActivity;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

import static android.app.Activity.RESULT_OK;

public class MessageFragment extends BaseFragment {

    private Button btn_search_person;
    private RecyclerView rv_conversationList;
    private ConversationAdapter conversationAdapter;

    private static final int REQUEST_CODE_SEARCH_PERSON = 0;

    private List<Conversation> conversationList = new ArrayList<>();

    @Override
    protected View initView() {
        View view = View.inflate(context, R.layout.fragment_message, null);
        btn_search_person = view.findViewById(R.id.btn_search_person);
        rv_conversationList = view.findViewById(R.id.rv_conversationList);
        btn_search_person.setOnClickListener(v -> {
            Intent intent = new Intent(context, SearchPersonActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SEARCH_PERSON);
        });

        conversationAdapter = new ConversationAdapter(context, conversationList);
        rv_conversationList.setAdapter(conversationAdapter);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        rv_conversationList.setLayoutManager(linearLayoutManager);
        rv_conversationList.addItemDecoration(
                new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        loadConversation();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SEARCH_PERSON && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            String json = data.getStringExtra("ConversationResponse");
            Conversation conversation = JSON.parseObject(json, Conversation.class);
            loadConversation();
        }
    }

    /**
     * 联网加载conversation列表数据
     */
    private void loadConversation() {
        HttpUtil.post("/conversation/pullAllConversations", null, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(context, "pullAllConversations onFailure "
                        + R.string.error_occurred_please_retry, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<List<Conversation>> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<List<Conversation>>>(Result.class) {
                        });
                if (result.getCode() != Code.SUCCESS) {
                    Toasty.error(context, result.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                //正常获取到了消息列表
                conversationList.clear();
                conversationList.addAll(result.getData());
                conversationAdapter.notifyDataSetChanged();
            }
        });
    }

}
