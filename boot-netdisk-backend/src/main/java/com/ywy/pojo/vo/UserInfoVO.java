package com.ywy.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息
 */
@Data
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 注册邮箱
     */
    private String email;

    /**
     * qq 头像
     */
    private String qqAvatar;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 总存储空间，单位byte
     */
    private Long totalSpace;

    /**
     * 已使用空间，单位byte
     */
    private Long usedSpace;

    /**
     * 0:禁用 1:正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
