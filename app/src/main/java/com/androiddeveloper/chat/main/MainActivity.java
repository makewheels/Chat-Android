package com.androiddeveloper.chat.main;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.common.Code;
import com.androiddeveloper.chat.common.Result;
import com.androiddeveloper.chat.login.LoginActivity;
import com.androiddeveloper.chat.login.UserInfoResponse;
import com.androiddeveloper.chat.main.download.DownloadActivity;
import com.androiddeveloper.chat.main.message.MessageFragment;
import com.androiddeveloper.chat.main.settings.SettingsFragment;
import com.androiddeveloper.chat.utils.FilePathUtil;
import com.androiddeveloper.chat.utils.LoginTokenUtil;
import com.androiddeveloper.chat.utils.UserUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    private RadioGroup rg_tabs;
    private RadioButton rb_message;
    private RadioButton rb_settings;

    private List<BaseFragment> fragmentList;
    private Fragment currentFragment;

    private PackageInfo packageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        checkVersion();
        deleteApk();
        checkLoginToken();

    }

    private void initView() {
        rg_tabs = findViewById(R.id.rg_tabs);
        rb_message = findViewById(R.id.rb_message);
        rb_settings = findViewById(R.id.rb_settings);

        setBounds(R.xml.tab_selector_message, rb_message);
        setBounds(R.xml.tab_selector_settings, rb_settings);
        rb_message.setButtonDrawable(new StateListDrawable());
        rb_settings.setButtonDrawable(new StateListDrawable());
        initFragments();

    }

    private void initFragments() {
        fragmentList = new ArrayList<>();
        fragmentList.add(new MessageFragment());
        fragmentList.add(new SettingsFragment());
        switchFragment(currentFragment, fragmentList.get(0));
        rg_tabs.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_message) {
                switchFragment(currentFragment, fragmentList.get(0));
            } else {
                switchFragment(currentFragment, fragmentList.get(1));
            }
        });

    }

    private void switchFragment(Fragment from, Fragment to) {
        if (from == to) {
            return;
        }
        currentFragment = to;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (!to.isAdded()) {
            if (from != null)
                transaction.hide(from);
            transaction.add(R.id.fl_content, to);
        } else {
            if (from != null)
                transaction.hide(from);
            transaction.show(to);
        }
        transaction.commit();
    }

    /**
     * 解决图标缩放显示不全问题
     */
    private void setBounds(int drawableId, RadioButton radioButton) {
        //定义底部标签图片大小和位置
        Drawable selector = getResources().getDrawable(drawableId);
        //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形  (这里的长和宽写死了 自己可以可以修改成 形参传入)
        int size = 100;
        selector.setBounds(0, 0, size, size);
        //设置图片在文字的哪个方向
        radioButton.setCompoundDrawables(null, selector, null, null);
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
                Toasty.error(MainActivity.this,
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
                //如果现在的版本是最新版
                if (packageInfo.versionCode == latestInfoResponse.getVersionCode())
                    return;
                //如果小于最新版，那就需要提示更新了
                if (packageInfo.versionCode < latestInfoResponse.getVersionCode()) {
                    promptUpdate(latestInfoResponse);
                }
                //如果大于最新版也是什么都不做
            }
        });
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
     * 打开下载页面
     *
     * @param latestInfoResponse
     */
    private void openDownloadActivity(LatestInfoResponse latestInfoResponse) {
        Intent intent = new Intent(this, DownloadActivity.class);
        intent.putExtra("LatestInfoResponse", JSON.toJSONString(latestInfoResponse));
        startActivity(intent);
    }

    private void checkLoginToken() {
        HttpUtil.post("/user/checkLoginToken", null, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(MainActivity.this,
                        "/user/checkLoginToken onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<Void> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<Void>>(Result.class) {
                        });
                //如果需要登录，跳转到登录Activity
                if (result.getCode() == Code.CHECK_LOGIN_TOKEN_ERROR) {
                    Toasty.error(MainActivity.this,
                            "checkLoginToken onResponse " + result.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    LoginTokenUtil.removeLoginToken();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    //如果loginToken检查没问题，获取用户信息
                    getUserInfo();
                }
            }
        });
    }

    /**
     * 获取用户信息，保存到全局范围内
     */
    private void getUserInfo() {
        HttpUtil.post("/user/getUserInfo", null, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Toasty.error(MainActivity.this,
                        "getUserInfo onFailure " + R.string.error_occurred_please_retry,
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Result<UserInfoResponse> result
                        = JSON.parseObject(response,
                        new TypeReference<Result<UserInfoResponse>>(Result.class) {
                        });
                UserInfoResponse userInfoResponse = result.getData();
                UserUtil.userId = userInfoResponse.getUserId();
                UserUtil.loginName = userInfoResponse.getLoginName();
                UserUtil.nickname = userInfoResponse.getNickname();
                UserUtil.headImageUrl = userInfoResponse.getHeadImageUrl();
                UserUtil.loginToken = userInfoResponse.getLoginToken();
                UserUtil.phone = userInfoResponse.getPhone();
                UserUtil.jpushRegistrationId = userInfoResponse.getJpushRegistrationId();
                UserUtil.createTime = userInfoResponse.getCreateTime();
            }
        });
    }
}