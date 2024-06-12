package com.ywy.pojo.entity;

import java.util.Date;

import com.ywy.core.base.BaseEntity;
import lombok.Data;

/**
 * 用户信息
 * @TableName sys_user
 */
@Data
public class SysUser extends BaseEntity {
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
     * QQ登录ID
     */
    private String qqOpenId;

    /**
     * QQ头像
     */
    private String qqAvatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 最后一次登录时间
     */
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
     * 状态（0：禁用；1：启用）
     */
    private Integer status;

    /**
     * 用户角色（0：普通用户；1：管理员）
     */
    private Integer role;

    @Override
    public void setId(String id) {
        super.setId(id);
        this.userId = id;
    }
}