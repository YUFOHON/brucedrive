package com.ywy.pojo.entity;

import com.ywy.core.base.BaseEntity;
import lombok.Data;

/**
 * 邮箱验证码
 * @TableName sys_email_code
 */
@Data
public class SysEmailCode extends BaseEntity {
    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 状态（0：未使用；1：已使用）
     */
    private Integer status;
}