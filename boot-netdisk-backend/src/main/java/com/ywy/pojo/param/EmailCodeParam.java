package com.ywy.pojo.param;

import com.ywy.core.base.BaseParam;
import lombok.Data;

@Data
public class EmailCodeParam extends BaseParam {
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
