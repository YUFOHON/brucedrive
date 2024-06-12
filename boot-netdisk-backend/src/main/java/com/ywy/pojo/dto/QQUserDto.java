package com.ywy.pojo.dto;

import lombok.Data;

/**
 * QQ用户信息
 */
@Data
public class QQUserDto {
    /**
     * 返回码，0成功
     */
    private Integer ret;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 用户在QQ空间的昵称
     */
    private String nickname;

    /**
     * QQ头像URL，40x40像素
     */
    private String figureurl_qq_1;

    /**
     * QQ头像URL，100x100像素，不一定存在
     */
    private String figureurl_qq_2;

    /**
     * 性别
     */
    private String gender;
}
