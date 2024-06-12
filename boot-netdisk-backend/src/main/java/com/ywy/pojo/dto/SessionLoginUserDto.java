package com.ywy.pojo.dto;

import lombok.Data;

/**
 * 登录用户session信息
 */
@Data
public class SessionLoginUserDto {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String nickName;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

    /**
     * 用户头像
     */
    private String avatar;
}
