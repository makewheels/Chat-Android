package com.androiddeveloper.chat.main.message.dialogperson;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.jpush.PullMessageResponse;
import com.androiddeveloper.chat.main.message.conversation.Conversation;
import com.androiddeveloper.chat.oss.CredentialProvider;
import com.androiddeveloper.chat.oss.UploadUtil;
import com.androiddeveloper.chat.utils.FilePathUtil;
import com.androiddeveloper.chat.utils.MessageType;
import com.androiddeveloper.chat.utils.Pcm2Wav;
import com.androiddeveloper.chat.utils.UserUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;
import com.permissionx.guolindev.PermissionX;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class DialogActivity extends AppCompatActivity {
    private static final int RC_CHOOSE_PHOTO = 0;

    private Conversation conversation;

    private TextView tv_nickname;
    private RecyclerView rv_dialog;

    private Button btn_audio;
    private Button btn_image;

    private EditText et_input;
    private Button btn_send;

    private MessageAdapter messageAdapter;

    public static final String ACTION_RECEIVE_PERSON_MESSAGE = "ACTION_RECEIVE_PERSON_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);

        initViews();

        Intent intent = getIntent();
        String conversationJson = intent.getStringExtra("Conversation");
        conversation = JSON.parseObject(conversationJson, Conversation.class);
        tv_nickname.setText(conversation.getTitle());

        addListeners();

    }

    private void initViews() {
        tv_nickname = findViewById(R.id.tv_nickname);
        rv_dialog = findViewById(R.id.rv_dialog);
        btn_send = findViewById(R.id.btn_send);
        et_input = findViewById(R.id.et_input);
        btn_audio = findViewById(R.id.btn_audio);
        btn_image = findViewById(R.id.btn_image);

        messageAdapter = new MessageAdapter(this, new ArrayList<>());
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv_dialog.setLayoutManager(linearLayoutManager);
        rv_dialog.setAdapter(messageAdapter);

        loadButtonLeftIcon(R.drawable.microphone, btn_audio);
        loadButtonLeftIcon(R.drawable.image, btn_image);
        loadButtonLeftIcon(R.drawable.send, btn_send);

        //收到推送消息的，广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVE_PERSON_MESSAGE);
        registerReceiver(new MessageReceiver(), filter);
    }

    //加载图片左边的图标
    private void loadButtonLeftIcon(int id, Button button) {
        Drawable drawable = getResources().getDrawable(id);
        drawable.setBounds(0, 0, 64, 64);
        button.setCompoundDrawables(drawable, null, null, null);
    }

    /**
     * 监听收到推送消息的广播
     */
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //收到人的消息
            if (action.equals(ACTION_RECEIVE_PERSON_MESSAGE)) {
                String messageId = intent.getStringExtra("messageId");
                String messageType = intent.getStringExtra("messageType");
                //拉取人的消息
                pullPersonMessage(messageId);
            }
        }

    }

    private void addListeners() {
        //发送消息按钮
        btn_send.setOnClickListener(v -> {
            String input = et_input.getText().toString();
            //检查输入内容是否为空
            if (StringUtils.isEmpty(input)) {
                Toasty.info(DialogActivity.this, R.string.please_input_content,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("conversationId", conversation.getConversationId());
            paramsMap.put("messageType", MessageType.TEXT);
            paramsMap.put("content", input);
            HttpUtil.post("/message/person/sendMessage", paramsMap, new CallBackUtil.CallBackString() {
                @Override
                public void onFailure(Call call, Exception e) {
                    Toasty.error(DialogActivity.this,
                            "person sendMessage onFailure " + R.string.error_occurred_please_retry,
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                @Override
                public void onResponse(String response) {
                    Result<SendMessageResponse> result
                            = JSON.parseObject(response,
                            new TypeReference<Result<SendMessageResponse>>(Result.class) {
                            });
                    if (result.getCode() != Code.SUCCESS) {
                        Toasty.error(DialogActivity.this, result.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //到这里，说明是发送成功了
                    //清空输入框
                    et_input.setText("");
                    //更新recycle view
                    SendMessageResponse sendMessageResponse = result.getData();
                    PersonMessage personMessage = new PersonMessage();
                    personMessage.setMessageId(sendMessageResponse.getMessageId());
                    personMessage.setConversationId(sendMessageResponse.getConversationId());
                    personMessage.setFromUserId(sendMessageResponse.getFromUserId());
                    personMessage.setToUserId(sendMessageResponse.getToUserId());
                    personMessage.setSenderHeadUrl(UserUtil.headImageUrl);
                    personMessage.setIsSend(true);
                    personMessage.setMessageType(MessageType.TEXT);
                    personMessage.setContent(input);
                    personMessage.setCreateTime(sendMessageResponse.getCreateTime());
                    addMessage(personMessage);
                }
            });
        });

        //语音按钮
        btn_audio.setOnClickListener(v -> {
            //检查权限
            PermissionX.init(this)
                    .permissions(Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (!allGranted) {
                            Toasty.error(DialogActivity.this, R.string.permission_denied_record_audio,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        recordAndSendAudio();
                    });
        });

        //图片按钮
        btn_image.setOnClickListener(v -> {
            File recordedFile = new File(getFilesDir(),
                    "/chat/user1a9a80964bc44183b72d36742742aa7d/audio/1615561782854.wav");

            new Thread(() -> {
                MediaPlayer recordedSong = MediaPlayer.create(
                        DialogActivity.this, Uri.fromFile(recordedFile));
                try {
                    recordedSong.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                recordedSong.start();
            }).start();
        });
    }

    private boolean isRecording = false;
    private File pcmFile;
    private final int sampleRateInHz = 16000;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    private AudioRecord audioRecord;

    /**
     * 录音
     */
    private void recordAudio() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig,
                audioFormat, bufferSize);
        isRecording = true;

        pcmFile = new File(FilePathUtil.getAudioFolder(), System.currentTimeMillis() + ".pcm");
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(pcmFile)));
            byte[] buffer = new byte[bufferSize];
            audioRecord.startRecording();
            while (isRecording
                    && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dataOutputStream.write(buffer[i]);
                }
            }
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //停止录音
    private void stopRecord() {
        isRecording = false;
        if (audioRecord != null) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                audioRecord.stop();
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
                audioRecord.release();
        }
        //文件转换
        String filename = FilenameUtils.getBaseName(pcmFile.getName()) + ".wav";
        //TODO 其实这里有问题，我录制的音频文件，应该放到最终位置，但是现在又不知道最终的文件名
        File wavFile = new File(pcmFile.getParent(), filename);
        Pcm2Wav.convert(pcmFile, wavFile, sampleRateInHz, audioFormat, 1, bufferSize);
        //删除pcm文件
        pcmFile.delete();
        pcmFile = null;

        //获取音频时长
        long duration = 0;

        //发语音消息
        String md5 = null;
        try {
            md5 = DigestUtils.md5Hex(new FileInputStream(wavFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("conversationId", conversation.getConversationId());
        paramsMap.put("messageType", MessageType.AUDIO);
        paramsMap.put("md5", md5);
        paramsMap.put("originalFilename", wavFile.getName());
        paramsMap.put("size", wavFile.length() + "");
        paramsMap.put("duration", duration + "");
        HttpUtil.post("/message/person/sendMessage", paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(DialogActivity.this,
                        "person sendMessage onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<SendMessageResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<SendMessageResponse>>(Result.class) {
                        });
                if (result.getCode() != Code.SUCCESS) {
                    Toasty.error(DialogActivity.this, result.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //到这里，说明是发送成功了
                SendMessageResponse sendMessageResponse = result.getData();
                //如果需要上传文件，则上传
                if (sendMessageResponse.getIsNeedUpload()) {
                    uploadAudio(sendMessageResponse, wavFile);
                }
            }
        });
    }

    //发送录音文件
    private void recordAndSendAudio() {
        //如果不在录音，那就开始录音
        if (!isRecording)
            new Thread() {
                @Override
                public void run() {
                    recordAudio();
                }
            }.start();
        else
            //如果正在录音，再次点击按钮，就停止录音
            stopRecord();
    }

    //上传文件
    private void uploadAudio(SendMessageResponse response, File file) {
        CredentialProvider credentialProvider
                = new CredentialProvider(response.getOssCredential());
        TransferManager transferManager
                = UploadUtil.getTransferManager(
                DialogActivity.this, response.getRegion(), credentialProvider);
        // 上传文件
        COSXMLUploadTask cosxmlUploadTask
                = transferManager.upload(response.getBucket(), response.getObject(),
                file.getPath(), null);

        //设置上传进度回调
        cosxmlUploadTask.setCosXmlProgressListener((complete, target) ->
                Log.e("tag", complete + " / " + target)
        );
        //设置返回结果回调
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult cOSXMLUploadTaskResult =
                        (COSXMLUploadTask.COSXMLUploadTaskResult) result;
            }

            @Override
            public void onFail(CosXmlRequest request,
                               CosXmlClientException clientException,
                               CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });
        //设置任务状态回调, 可以查看任务过程
        cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                //当上传完成时，通知应用服务器
                if (state == TransferState.COMPLETED) {
                    onUploadFileFinish(response, file);
                }
            }
        });
    }

    /**
     * 当上传文件完成时
     *
     * @param response
     * @param file
     */
    private void onUploadFileFinish(SendMessageResponse response, File file) {
        Map<String, String> paramsMap = new HashMap<>();
        String messageId = response.getMessageId();
        paramsMap.put("messageId", messageId);
        HttpUtil.post("/message/person/uploadFileFinish", paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(DialogActivity.this,
                        "person uploadFileFinish onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<SendMessageResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<SendMessageResponse>>(Result.class) {
                        });
                if (result.getCode() != Code.SUCCESS) {
                    Toasty.error(DialogActivity.this, result.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                //通知成功，那么现在上传文件都完成了

                //更新recycle view
//                PersonMessage personMessage = new PersonMessage();
//                personMessage.setMessageId(sendMessageResponse.getMessageId());
//                personMessage.setConversationId(sendMessageResponse.getConversationId());
//                personMessage.setFromUserId(sendMessageResponse.getFromUserId());
//                personMessage.setToUserId(sendMessageResponse.getToUserId());
//                personMessage.setSenderHeadUrl(UserUtil.headImageUrl);
//                personMessage.setIsSend(true);
//                personMessage.setMessageType(MessageType.AUDIO);
//                personMessage.setCreateTime(sendMessageResponse.getCreateTime());
//                addMessage(personMessage);
            }
        });
    }


    /**
     * 判断消息类型是不是文件
     */
    public boolean isFileTypeMessage(String messageType) {
        return messageType.equals(MessageType.AUDIO)
                || messageType.equals(MessageType.IMAGE)
                || messageType.equals(MessageType.VIDEO);
    }

    /**
     * 更新界面
     *
     * @param personMessage
     */
    private void updateDialog(PersonMessage personMessage) {
        //添加到dialog里
        messageAdapter.addMessage(personMessage);
        //滑动到最低端
        rv_dialog.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    public void addMessage(PersonMessage personMessage) {
        //判断类型，看是否需要下载
        String messageType = personMessage.getMessageType();
        //如果不需要下载，直接更新界面
        if (!isFileTypeMessage(messageType)) {
            updateDialog(personMessage);
        } else {
            //需要下载文件
            //判断消息类型
            if (messageType.equals(MessageType.AUDIO)) {
                //先下载，等待下载完成后，再更新界面
                new Thread(() -> {
                    try {
                        FileUtils.copyURLToFile(new URL(personMessage.getFileUrl()),
                                FilePathUtil.getFile(personMessage));
                        runOnUiThread(() -> updateDialog(personMessage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        //写入数据库

    }

    /**
     * 拉取对个人发的消息
     *
     * @param messageId
     */
    public void pullPersonMessage(String messageId) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("messageId", messageId);
        HttpUtil.post("/message/person/pullByMessageId", paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(DialogActivity.this,
                        "/message/person/pullByMessageId onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<PullMessageResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<PullMessageResponse>>(Result.class) {
                        });
                PullMessageResponse data = result.getData();
                PersonMessage personMessage = new PersonMessage();
                personMessage.setMessageId(data.getMessageId());
                personMessage.setConversationId(data.getConversationId());
                personMessage.setFromUserId(data.getFromUserId());
                personMessage.setToUserId(data.getToUserId());
                personMessage.setSenderHeadUrl(conversation.getHeadImageUrl());
                personMessage.setIsSend(false);
                personMessage.setMessageType(data.getMessageType());
                personMessage.setContent(data.getContent());
                personMessage.setCreateTime(data.getCreateTime());

                personMessage.setFileUrl(data.getFileUrl());
                personMessage.setFileName(data.getFileName());
                personMessage.setImagePreviewUrl(data.getImagePreviewUrl());

                //TODO 这里应该有一个判断，如果打开了这个conversation，那就添加到界面上
                //TODO 写入数据库
                addMessage(personMessage);
                //TODO 其实这里还应该干一件事，上报已达
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == RC_CHOOSE_PHOTO) {
        }

    }

}