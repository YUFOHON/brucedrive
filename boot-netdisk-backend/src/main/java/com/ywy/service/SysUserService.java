package com.ywy.service;

import com.ywy.core.base.BaseService;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.entity.SysUser;
import com.ywy.pojo.param.UserParam;

/**
* @author ywy
* @description 针对表【sys_user(用户信息)】的数据库操作Service
* @createDate 2023-07-19 09:48:00
*/
public interface SysUserService extends BaseService<SysUser, UserParam> {
    /**
     * 注册
     * @param email
     * @param nickName
     * @param password
     * @param emailCode
     */
    void register(String email, String nickName, String password, String emailCode);

    /**
     * 登录
     * @param email
     * @param password
     * @return
     */
    SessionLoginUserDto login(String email, String password);

    /**
     * 重置用户密码
     * @param email
     * @param password
     * @param emailCode
     */
    void resetPwd(String email, String password, String emailCode);

    /**
     * 根据用户ID更新用户
     * @param updateUser
     * @return
     */
    int updateByUserId(SysUser updateUser);

    /**
     * QQ登录
     * @param code
     * @return
     */
    SessionLoginUserDto qqLogin(String code);

    /**
     * 修改用户状态
     * @param userId
     * @param status
     */
    void updateUserStatus(String userId, Integer status);

    /**
     * 修改用户空间
     * @param userId
     * @param changeSpace
     */
    void changeUserSpace(String userId, Integer changeSpace);

    /**
     * 根据ID查询
     * @param userId
     * @return
     */
    SysUser selectByUserId(String userId);
}
