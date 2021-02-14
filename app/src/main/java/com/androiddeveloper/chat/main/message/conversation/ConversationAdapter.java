package com.androiddeveloper.chat.main.message.conversation;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.main.message.dialog.DialogueActivity;
import com.bumptech.glide.Glide;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {
    private final Context context;
    private List<Conversation> conversationList;

    public ConversationAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_conversation, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.tv_nickName.setText(conversation.getTitle());
        Glide.with(context).load(conversation.getHeadImageUrl()).into(holder.iv_head);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iv_head;
        private final TextView tv_nickName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_head = itemView.findViewById(R.id.iv_head);
            tv_nickName = itemView.findViewById(R.id.tv_nickName);
            itemView.setOnClickListener(v -> {
                int position = getLayoutPosition();
                Intent intent = new Intent(context, DialogueActivity.class);
                Conversation conversation = conversationList.get(position);
                intent.putExtra("Conversation", JSON.toJSONString(conversation));
                context.startActivity(intent);
            });
        }
    }

}
