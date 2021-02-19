package com.androiddeveloper.chat.login;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.main.LatestInfoResponse;
import com.androiddeveloper.chat.main.MainActivity;
import com.androiddeveloper.chat.main.download.DownloadActivity;
import com.androiddeveloper.chat.register.RegisterActivity;
import com.androiddeveloper.chat.utils.Constants;
import com.androiddeveloper.chat.utils.FilePathUtil;
import com.androiddeveloper.chat.utils.LoginTokenUtil;
import com.androiddeveloper.chat.utils.SharedPreferencesUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {
    private EditText et_loginName;
    private EditText et_password;
    private Button btn_login;
    private Button btn_to_register;

    private static final int REQUEST_CODE_START_REGISTER_ACTIVITY = 0;

    private PackageInfo packageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        setClickListener();

        //这是第一个页面，要在这里检查版本
        checkVersion();
        deleteApk();

        //看有没有loginToken
        String loginToken = LoginTokenUtil.getLoginToken();
        //如果有loginToken，跳转主页面
        if (StringUtils.isNotEmpty(loginToken)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initView() {
        et_loginName = findViewById(R.id.et_loginName);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_to_register = findViewById(R.id.btn_to_register);
    }

    private void setClickListener() {
        btn_login.setOnClickListener(v -> click_btn_login());
        btn_to_register.setOnClickListener(v -> {
            //前往注册页面的时候，要一个结果
            //一种情况是用户直接返回，不需要任何操作
            //还有一种情况是用户在注册页注册成功，那就要finish掉登录Activity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REQUEST_CODE_START_REGISTER_ACTIVITY);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == REQUEST_CODE_START_REGISTER_ACTIVITY) {
            finish();
        }
    }

    /**
     * 检查版本
     */
    private void checkVersion() {
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //发送请求检查版本
        HttpUtil.post("/app/getLatestInfo", null, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(LoginActivity.this,
                        "/app/getLatestInfo onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<LatestInfoResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<LatestInfoResponse>>(Result.class) {
                        });
                if (result.getCode() != Code.SUCCESS)
                    return;
                //和我现在的版本号对比
                LatestInfoResponse latestInfoResponse = result.getData();
                //如果小于最新版，那就需要提示更新了
                if (packageInfo.versionCode < latestInfoResponse.getVersionCode()) {
                    promptUpdate(latestInfoResponse);
                }
            }
        });
    }

    /**
     * 提示更新
     */
    private void promptUpdate(LatestInfoResponse latestInfoResponse) {
        //拼接描述信息
        String message = packageInfo.versionName + " > " + latestInfoResponse.getVersionName()
                + "\n" + getString(R.string.size) + ": "
                + FileUtils.byteCountToDisplaySize(latestInfoResponse.getApkSize());
        String description = latestInfoResponse.getDescription();
        if (description != null)
            message += "\n" + description;

        //如果不是强制更新
        if (!latestInfoResponse.getIsForceUpdate()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.please_update)
                    .setMessage(message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        //打开下载页面
                        openDownloadActivity(latestInfoResponse);
                        finish();
                    })
                    .setNegativeButton(R.string.no, null)
                    .create().show();
        } else {
            //如果是强制更新
            new AlertDialog.Builder(this)
                    .setTitle(R.string.please_update)
                    .setMessage(message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        //打开下载页面
                        openDownloadActivity(latestInfoResponse);
                        finish();
                    })
                    .setCancelable(false)
                    .create().show();
        }
    }

    /**
     * 删除，比当前版本低的apk文件
     */
    private void deleteApk() {
        File folder = new File(FilePathUtil.getApkDownloadFolder(this));
        File[] files = folder.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            String baseName = FilenameUtils.getBaseName(file.getName());
            int versionCode;
            //解析版本号，如果解析错误，就跳过吧
            try {
                versionCode = Integer.parseInt(baseName.split("-")[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }
            //如果apk文件的版本号，小于我当前的版本号，那就删除
            if (versionCode < packageInfo.versionCode) {
                file.delete();
            }
        }
    }

    /**
     * 打开下载页面
     *
     * @param latestInfoResponse
     */
    private void openDownloadActivity(LatestInfoResponse latestInfoResponse) {
        Intent intent = new Intent(this, DownloadActivity.class);
        intent.putExtra("LatestInfoResponse", JSON.toJSONString(latestInfoResponse));
        startActivity(intent);
    }

    /**
     * 登录按钮
     */
    private void click_btn_login() {
        btn_login.setEnabled(false);
        String loginName = et_loginName.getText().toString();
        String password = et_password.getText().toString();
        String jpushRegistrationId = JPushInterface.getRegistrationID(this);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("loginName", loginName);
        paramsMap.put("password", password);
        paramsMap.put("jpushRegistrationId", jpushRegistrationId);
        HttpUtil.post("/user/login", paramsMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                btn_login.setEnabled(true);
                Toasty.error(LoginActivity.this, R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT, true).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                btn_login.setEnabled(true);
                Result<UserInfoResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<UserInfoResponse>>(Result.class) {
                        });
                int code = result.getCode();
                //如果有错误
                if (code != Code.SUCCESS) {
                    Toasty.error(LoginActivity.this, result.getMessage()).show();
                    return;
                }
                //如果没有错误
                //到这说明已经注册成功了，保存用户信息
                UserInfoResponse userInfoResponse = result.getData();
                if (userInfoResponse != null) {
                    LoginTokenUtil.saveLoginToken(userInfoResponse.getLoginToken());
                    SharedPreferencesUtil.putString(Constants.KEY_USER_ID, userInfoResponse.getUserId());
                    SharedPreferencesUtil.putString(Constants.KEY_LOGIN_NAME, userInfoResponse.getLoginName());
                    SharedPreferencesUtil.putString(Constants.KEY_HEAD_IMAGE_URL, userInfoResponse.getHeadImageUrl());
                }
                Toasty.success(LoginActivity.this, R.string.login_success).show();
                // 跳转到主页面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("data", response);
                startActivity(intent);
                finish();
            }
        });
    }
}