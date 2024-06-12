package com.ywy.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP请求工具类
 */
@Slf4j
public class OkHttpUtil {
    private static final int TIME_OUT_SECONDS = 5;

    private static OkHttpClient.Builder getClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false)
                .retryOnConnectionFailure(false)
                .connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        return builder;
    }

    private static Request.Builder getRequestBuilder(Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        if (null != headers) {
            for (Map.Entry<String, String> map : headers.entrySet()) {
                String key = map.getKey();
                String value;
                if (null == map.getValue()) {
                    value = "";
                } else {
                    value = map.getKey();
                }
                builder.addHeader(key, value);
            }
        }
        return builder;
    }

    /**
     * get请求
     * @param url
     * @return
     */
    public static String get(String url) {
        ResponseBody responseBody = null;
        try {
            OkHttpClient.Builder clientBuilder = getClientBuilder();
            Request.Builder requestBuilder = getRequestBuilder(null);
            OkHttpClient client = clientBuilder.build();
            Request request = requestBuilder.url(url).build();
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            String responseStr = responseBody.string();
            log.info("请求地址：{}，返回谢谢：{}", url, request);

            return responseStr;
        } catch (Exception e) {
            log.error("请求异常", e);
            return null;
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }
}
