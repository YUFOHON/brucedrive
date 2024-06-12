package com.ywy.mapper;

import com.ywy.core.base.BaseMapper;
import com.ywy.pojo.entity.SysEmailCode;
import com.ywy.pojo.param.EmailCodeParam;
import org.apache.ibatis.annotations.Param;

/**
* @author ywy
* @description 针对表【sys_email_code(邮箱验证码)】的数据库操作Mapper
* @createDate 2023-07-19 19:13:53
* @Entity com.ywy.pojo.com.ywy.pojo.entity.SysEmailCode
*/
public interface SysEmailCodeMapper extends BaseMapper<SysEmailCode, EmailCodeParam> {
    /**
     * 将邮箱验证码置为无效
     * @param email
     */
    void disableCode(@Param("email") String email);

    /**
     * 查询邮箱验证码
     * @param email
     * @param code
     * @return
     */
    SysEmailCode selectByEmailAndCode(@Param("email") String email, @Param("code") String code);
}




