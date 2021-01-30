package com.androiddeveloper.chat.utils;

public class LoginTokenUtil {
    public static String KEY_LOGIN_TOKEN = "loginToken";
    private static String loginToken;

    static {
        loginToken = SharedPreferencesUtil.getString(LoginTokenUtil.KEY_LOGIN_TOKEN);
    }

    /**
     * 获取loginToken
     *
     * @return
     */
    public static String getLoginToken() {
        return loginToken;
    }

    /**
     * 设置loginToken并保存到sp
     *
     * @param loginToken
     */
    public static void saveLoginToken(String loginToken) {
        LoginTokenUtil.loginToken = loginToken;
        SharedPreferencesUtil.putString(LoginTokenUtil.KEY_LOGIN_TOKEN, loginToken);
    }

}
