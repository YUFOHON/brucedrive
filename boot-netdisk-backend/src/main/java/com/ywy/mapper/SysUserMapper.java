package com.ywy.mapper;

import com.ywy.core.base.BaseMapper;
import com.ywy.pojo.entity.SysEmailCode;
import com.ywy.pojo.entity.SysUser;
import com.ywy.pojo.param.EmailCodeParam;
import com.ywy.pojo.param.UserParam;
import org.apache.ibatis.annotations.Param;

/**
* @author ywy
* @description 针对表【sys_user(用户信息)】的数据库操作Mapper
* @createDate 2023-07-19 09:48:00
* @Entity com.ywy.domain.SysUser
*/
public interface SysUserMapper extends BaseMapper<SysUser, UserParam> {
    /**
     * 根据邮箱查询用户
     * @param email
     * @return
     */
    SysUser selectByEmail(@Param("email") String email);

    /**
     * 根据昵称查询用户
     * @param nickName
     * @return
     */
    SysUser selectByNickName(@Param("nickName") String nickName);

    /**
     * 根据qq openId查询用户
     * @param openId
     * @return
     */
    SysUser selectByQqOpenId(@Param("openId") String openId);

    /**
     * 根據郵箱更新用戶
     * @param updateUser
     */
    int updateByEmail(SysUser updateUser);

    /**
     * 根據qq openId更新用戶
     * @param updateUser
     * @return
     */
    int updateByQqOpenId(SysUser updateUser);

    /**
     * 更新使用者使用空間
     * @param userId
     * @param addUsedSpace
     * @param addTotalSpace
     * @return
     */
    int updateUserSpace(@Param("userId") String userId, @Param("addUsedSpace") Long addUsedSpace, @Param("addTotalSpace") Long addTotalSpace);

}




