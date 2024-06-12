package com.ywy.pojo.dto;

import lombok.Data;

/**
 * 系统设置
 */
@Data
public class SystemSettingsDto {
    /**
     * 用户注册邮箱验证码邮件标题
     */
    private String registerEmailTitle;

    /**
     * 用户注册邮箱验证码邮件内容
     */
    private String registerEmailContent;

    /**
     * 用户网盘初始化空间大小，单位MB
     */
    private Integer initUserSpace;
}
