package com.ywy.util;

import com.alibaba.fastjson.JSONObject;
import com.ywy.core.config.QQLoginConfig;
import com.ywy.core.exception.BusinessException;
import com.ywy.pojo.dto.QQUserDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.util.Map;

/**
 * QQ登录相关接口请求
 */
@Slf4j
public class QQLoginApi {
    /**
     * 获取access token
     * @param code
     * @param qqLoginConfig
     * @return
     */
    public static String getAccessToken(String code, QQLoginConfig qqLoginConfig) {
        String url = null;
        try {
            url = String.format(qqLoginConfig.getAccessTokenUrl(), qqLoginConfig.getAppId(), qqLoginConfig.getAppKey(), code, URLEncoder.encode(qqLoginConfig.getLoginCallbackUrl(), "utf-8"));
        } catch (Exception e) {
            log.error("URLEncoder异常", e);
        }

        // 发送请求
        String response = OkHttpUtil.get(url);
        if (response == null || response.indexOf("result") != -1) {
            log.error("获取qq access token失败：{}", response);
            throw new BusinessException("获取qq access token失败");
        }

        String accessToken = null;
        // 解析参数
        String[] params = response.split("&");
        if (params != null && params.length > 0) {
            for (String param : params) {
                if (param.indexOf("access_token") != -1) {
                    accessToken = param.split("=")[1];
                    break;
                }
            }
        }
        return accessToken;
    }

    /**
     * 获取openId
     * @param accessToken
     * @param qqLoginConfig
     * @return
     */
    public static String getOpenId(String accessToken, QQLoginConfig qqLoginConfig) {
        String url = String.format(qqLoginConfig.getOpenidUrl(), accessToken);
        String response = OkHttpUtil.get(url);

        String jsonStr = null;
        if (StringUtils.isNotBlank(response)) {
            int pos = response.indexOf("callback");
            if (pos != -1) {
                int start = response.indexOf("{");
                int end = response.indexOf("}");
                jsonStr = response.substring(start + 1, end - 1);
            }
        }
        if (null == jsonStr) {
            log.error("获取qq openId失败：{}", response);
            throw new BusinessException("获取qq openId失败");
        }

        Map map = JSONObject.parseObject(jsonStr, Map.class);
        if (map == null || map.containsKey("result")) {
            log.error("获取qq openId失败：{}", map);
            throw new BusinessException("获取qq openId失败");
        }

        return String.valueOf(map.get("openid"));
    }

    /**
     * 获取用户信息
     * @param accessToken
     * @param openId
     * @param qqLoginConfig
     * @return
     */
    public static QQUserDto getUserInfo(String accessToken, String openId, QQLoginConfig qqLoginConfig) {
        String url = String.format(qqLoginConfig.getUserInfoUrl(), accessToken, qqLoginConfig.getAppId(), openId);
        String response = OkHttpUtil.get(url);
        if (StringUtils.isBlank(response)) {
            log.error("获取qq用户信息失败");
            throw new BusinessException("获取qq用户信息失败");
        }
        QQUserDto qqUserDto = JSONObject.parseObject(response, QQUserDto.class);
        if (qqUserDto.getRet() != 0) {
            log.error("获取qq用户信息失败：{}", response);
            throw new BusinessException("获取qq用户信息失败");
        }
        return qqUserDto;
    }
}
