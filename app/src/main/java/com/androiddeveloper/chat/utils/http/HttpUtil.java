package com.androiddeveloper.chat.utils.http;

import com.androiddeveloper.chat.utils.Constants;
import com.androiddeveloper.chat.utils.LoginTokenUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 发请求前，在header里带上loginToken
 */
public class HttpUtil {
    public static void get(String url, CallBackUtil<String> callBack) {
        url = Constants.SERVER + url;
        //如果有loginToken，放入header
        String loginToken = LoginTokenUtil.getLoginToken();
        if (loginToken != null) {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put(LoginTokenUtil.KEY_LOGIN_TOKEN, loginToken);
            OkhttpUtil.get(url, null, headerMap, callBack);
        } else {
            OkhttpUtil.get(url, callBack);
        }
    }

    public static void post(String url, Map<String, String> paramsMap, CallBackUtil<String> callBack) {
        url = Constants.SERVER + url;
        //如果有loginToken，放入header
        String loginToken = LoginTokenUtil.getLoginToken();
        if (loginToken != null) {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put(LoginTokenUtil.KEY_LOGIN_TOKEN, loginToken);
            OkhttpUtil.post(url, paramsMap, headerMap, callBack);
        } else {
            OkhttpUtil.post(url, paramsMap, callBack);
        }
    }
}
