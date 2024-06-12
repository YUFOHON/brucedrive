package com.ywy.core.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

/**
 * System configuration
 */
@Data
@Component
@ConfigurationProperties(prefix = "system")
public class SystemConfig {
    /**
     * Email verification code sent to user
     */
    @Value("${spring.mail.username}")
    private String fromMail;

    /**
     * User registration email verification code email title
     */
    private String registerEmailTitle;

    /**
     * User registration email verification code email content
     */
    private String registerEmailContent;

    /**
     * Network disk initialization space, unit MB
     */
    private Integer initUserSpace;

    /**
     * File root directory
     */
    private String rootPath;
}