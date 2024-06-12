package com.ywy.service;

import com.ywy.core.base.BaseService;
import com.ywy.pojo.entity.SysEmailCode;
import com.ywy.pojo.param.EmailCodeParam;

/**
* @author ywy
* @description 针对表【sys_email_code(邮箱验证码)】的数据库操作Service
* @createDate 2023-07-19 19:13:53
*/
public interface SysEmailCodeService extends BaseService<SysEmailCode, EmailCodeParam> {
    /**
     * 发送邮箱验证码
     * @param email
     * @param type 0登录
     */
    void sendEmailCode(String email, Integer type);

    /**
     * 校验邮箱验证码
     * @param email
     * @param code
     */
    void checkEmailCode(String email, String code);
}
