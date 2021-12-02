package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        //创建OkHttp实例
        OkHttpClient client = new OkHttpClient();
        //想要发起请求，就要创建Request对象
        Request request = new Request.Builder().url(address).build();
        //创建Call对象，用他的方法发送请求并获取服务器返回数据在一个Response对象中
        client.newCall(request).enqueue(callback);
    }
}
