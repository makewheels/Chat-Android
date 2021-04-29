package com.androiddeveloper.chat.main.message.dialogperson;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.utils.FilePathUtil;
import com.androiddeveloper.chat.utils.MessageType;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private List<PersonMessage> messageList;

    public MessageAdapter(Context context, List<PersonMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    /**
     * 添加一条消息进来
     *
     * @param personMessage
     * @return
     */
    public List<PersonMessage> addMessage(PersonMessage personMessage) {
        messageList.add(personMessage);
        notifyItemInserted(messageList.size() - 1);
        return messageList;
    }

    /**
     * 删除一条消息
     *
     * @param personMessage
     * @return
     */
    public List<PersonMessage> removeMessage(PersonMessage personMessage) {
        messageList.remove(personMessage);
        notifyDataSetChanged();
        return messageList;
    }

    /**
     * 根据消息类型，返回item类型，并且还要判断是左边还是右边
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        PersonMessage personMessage = messageList.get(position);
        String type = personMessage.getMessageType();
        Boolean isSend = personMessage.getIsSend();
        if (type.equals(MessageType.TEXT)) {
            if (isSend)
                return MessageType.RC_TYPE.TEXT_RIGHT.hashCode();
            else
                return MessageType.RC_TYPE.TEXT_LEFT.hashCode();
        } else if (type.equals(MessageType.IMAGE)) {
            if (isSend)
                return MessageType.RC_TYPE.IMAGE_RIGHT.hashCode();
            else
                return MessageType.RC_TYPE.IMAGE_LEFT.hashCode();
        } else if (type.equals(MessageType.AUDIO)) {
            if (isSend)
                return MessageType.RC_TYPE.AUDIO_RIGHT.hashCode();
            else
                return MessageType.RC_TYPE.AUDIO_LEFT.hashCode();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //文字
        if (viewType == MessageType.RC_TYPE.TEXT_LEFT.hashCode())
            return new TextLeftHolder(View.inflate(context, R.layout.item_message_text_left, null));
        else if (viewType == MessageType.RC_TYPE.TEXT_RIGHT.hashCode())
            return new TextRightHolder(View.inflate(context, R.layout.item_message_text_right, null));
            //音频
        else if (viewType == MessageType.RC_TYPE.AUDIO_LEFT.hashCode())
            return new AudioLeftHolder(View.inflate(context, R.layout.item_message_audio_left, null));
        else if (viewType == MessageType.RC_TYPE.AUDIO_RIGHT.hashCode())
            return new AudioRightHolder(View.inflate(context, R.layout.item_message_audio_right, null));
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PersonMessage personMessage = messageList.get(position);
        //文本消息
        if (holder instanceof TextLeftHolder) {
            TextLeftHolder textLeftHolder = (TextLeftHolder) holder;
            textLeftHolder.tv_text.setText(personMessage.getContent());
            Glide.with(context).load(personMessage.getSenderHeadUrl()).into(textLeftHolder.iv_head);
        } else if (holder instanceof TextRightHolder) {
            TextRightHolder textRightHolder = (TextRightHolder) holder;
            textRightHolder.tv_text.setText(personMessage.getContent());
            Glide.with(context).load(personMessage.getSenderHeadUrl()).into(textRightHolder.iv_head);
        }
        //音频消息
        else if (holder instanceof AudioLeftHolder) {
            AudioLeftHolder audioLeftHolder = (AudioLeftHolder) holder;
            audioLeftHolder.tv_text.setText(personMessage.getFileName());
            audioLeftHolder.personMessage = personMessage;
            Glide.with(context).load(personMessage.getSenderHeadUrl()).into(audioLeftHolder.iv_head);
        } else if (holder instanceof AudioRightHolder) {
            AudioRightHolder audioRightHolder = (AudioRightHolder) holder;
            audioRightHolder.tv_text.setText(personMessage.getFileName());
            Glide.with(context).load(personMessage.getSenderHeadUrl()).into(audioRightHolder.iv_head);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class TextLeftHolder extends RecyclerView.ViewHolder {
        TextView tv_text;
        ImageView iv_head;

        public TextLeftHolder(@NonNull View itemView) {
            super(itemView);
            iv_head = itemView.findViewById(R.id.iv_head);
            tv_text = itemView.findViewById(R.id.tv_text);
        }
    }

    class TextRightHolder extends RecyclerView.ViewHolder {
        TextView tv_text;
        ImageView iv_head;

        public TextRightHolder(@NonNull View itemView) {
            super(itemView);
            iv_head = itemView.findViewById(R.id.iv_head);
            tv_text = itemView.findViewById(R.id.tv_text);
        }
    }

    class AudioLeftHolder extends RecyclerView.ViewHolder {
        TextView tv_text;
        ImageView iv_head;
        PersonMessage personMessage;

        public AudioLeftHolder(@NonNull View itemView) {
            super(itemView);
            iv_head = itemView.findViewById(R.id.iv_head);
            tv_text = itemView.findViewById(R.id.tv_text);
            tv_text.setOnClickListener(v -> {
                File file = FilePathUtil.getFile(personMessage);
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(file.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            });
        }
    }

    class AudioRightHolder extends RecyclerView.ViewHolder {
        TextView tv_text;
        ImageView iv_head;

        public AudioRightHolder(@NonNull View itemView) {
            super(itemView);
            iv_head = itemView.findViewById(R.id.iv_head);
            tv_text = itemView.findViewById(R.id.tv_text);
        }
    }

}
