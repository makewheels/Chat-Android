package com.androiddeveloper.chat.main;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
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
import com.androiddeveloper.chat.main.message.MessageFragment;
import com.androiddeveloper.chat.main.settings.SettingsFragment;
import com.androiddeveloper.chat.utils.LoginTokenUtil;
import com.androiddeveloper.chat.utils.MyInfoUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.HttpUtil;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        checkLoginToken();


    }

    private void initView() {
        rg_tabs = findViewById(R.id.rg_tabs);
        rb_message = findViewById(R.id.rb_message);
        rb_settings = findViewById(R.id.rb_settings);

        setBounds(R.xml.tab_selector_message, rb_message);
        setBounds(R.xml.tab_selector_settings, rb_settings);

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
        Drawable selector= getResources().getDrawable(drawableId);
        //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形  (这里的长和宽写死了 自己可以可以修改成 形参传入)
        int size = 100;
        selector.setBounds(0, 0, size, size);
        //设置图片在文字的哪个方向
        radioButton.setCompoundDrawables(null, selector, null, null);
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
                MyInfoUtil.userId = userInfoResponse.getUserId();
                MyInfoUtil.loginName = userInfoResponse.getLoginName();
                MyInfoUtil.nickname = userInfoResponse.getNickname();
                MyInfoUtil.headImageUrl = userInfoResponse.getHeadImageUrl();
                MyInfoUtil.loginToken = userInfoResponse.getLoginToken();
                MyInfoUtil.phone = userInfoResponse.getPhone();
                MyInfoUtil.jpushRegistrationId = userInfoResponse.getJpushRegistrationId();
                MyInfoUtil.createTime = userInfoResponse.getCreateTime();
            }
        });
    }
}