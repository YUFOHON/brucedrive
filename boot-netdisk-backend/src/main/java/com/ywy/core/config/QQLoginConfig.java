package com.ywy.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "qq-login")
public class QQLoginConfig {
    private String appId;
    private String appKey;
    private String authorizeUrl;
    private String accessTokenUrl;
    private String openidUrl;
    private String userInfoUrl;
    private String loginCallbackUrl;
}
