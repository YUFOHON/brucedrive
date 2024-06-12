package com.ywy.pojo.param;

import com.ywy.core.base.BaseParam;
import lombok.Data;

@Data
public class UserParam extends BaseParam {
    /**
     * 用户ID
     */
    private Integer userId;

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
     * 状态（0：禁用；1：启用）
     */
    private Integer status;


    /**
     * 昵称模糊查询
     */
    private String nickNameFuzzy;
}
