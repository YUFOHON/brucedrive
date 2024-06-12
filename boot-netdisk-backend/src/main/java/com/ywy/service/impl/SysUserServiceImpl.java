package com.ywy.service.impl;

import com.ywy.core.base.BaseServiceImpl;
import com.ywy.cache.SystemCache;
import com.ywy.core.constants.SystemConstants;
import com.ywy.core.config.QQLoginConfig;
import com.ywy.core.exception.BusinessException;
import com.ywy.mapper.SysFileInfoMapper;
import com.ywy.mapper.SysUserMapper;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.dto.QQUserDto;
import com.ywy.pojo.dto.UserSpaceDto;
import com.ywy.pojo.entity.SysUser;
import com.ywy.pojo.enums.UserStatusEnum;
import com.ywy.pojo.param.UserParam;
import com.ywy.service.SysEmailCodeService;
import com.ywy.service.SysUserService;
import com.ywy.util.QQLoginApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author bruce
* @description Database operation Service implementation for table [sys_user (user information)]
* @createDate 2024-05-30 09:48:00
*/
@Slf4j
@Service
public class SysUserServiceImpl extends BaseServiceImpl<SysUser, UserParam> implements SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysFileInfoMapper sysFileInfoMapper;
    @Resource
    private SysEmailCodeService sysEmailCodeService;

    @Resource
    private QQLoginConfig qqLoginConfig;
    @Resource
    private SystemCache systemCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password, String emailCode) {
        SysUser user = sysUserMapper.selectByEmail(email);
        if (null != user) {
            throw new BusinessException("EMAIL ALREADY EXISTS");
        }
        SysUser user2 = sysUserMapper.selectByNickName(nickName);
        if (null != user2) {
            throw new BusinessException("Nick name already exists");
        }

        // 校验邮箱验证码
        sysEmailCodeService.checkEmailCode(email, emailCode);

        // 生成用户ID
        String userId = RandomStringUtils.random(SystemConstants.LENGTH_10, false, true);
        SysUser saveUser = new SysUser();
        saveUser.setId(userId);
        saveUser.setNickName(nickName);
        saveUser.setEmail(email);
        saveUser.setPassword(DigestUtils.md5Hex(password));
        saveUser.setCreateTime(new Date());
        saveUser.setUsedSpace(0L);
        saveUser.setTotalSpace(systemCache.getSystemSettings().getInitUserSpace() * SystemConstants.MB);
        sysUserMapper.insert(saveUser);
    }

    @Override
    public SessionLoginUserDto login(String email, String password) {
        // 校验用户
        SysUser user = sysUserMapper.selectByEmail(email);
        if (null == user || !user.getPassword().equals(password)) {
            throw new BusinessException("account or password wrong");
        }
        if (UserStatusEnum.DISABLE.getStatus() == user.getStatus()) {
            throw new BusinessException("account is disabled");
        }

        String userId = user.getId();
        // update login time
        SysUser updateUser = new SysUser();
        updateUser.setLastLoginTime(new Date());
        sysUserMapper.updateById(updateUser, userId);

        SessionLoginUserDto loginUserDto = new SessionLoginUserDto();
        loginUserDto.setUserId(userId);
        loginUserDto.setNickName(user.getNickName());
        // Determine whether it is the administrator's email address
        loginUserDto.setIsAdmin(user.getRole() == 1);

        // Query the space used by the user
        Long usedSpace = sysFileInfoMapper.selectUsedSpace(userId);
        // Save user space information to redis cache
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUsedSpace(usedSpace);
        userSpaceDto.setTotalSpace(user.getTotalSpace());

        systemCache.saveUserSpace(userId, userSpaceDto);

        return loginUserDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(String email, String password, String emailCode) {
        // 校验用户
        SysUser user = sysUserMapper.selectByEmail(email);
        if (null == user) {
            throw new BusinessException("邮箱账号不存在");
        }

        //　更新用户密码
        sysEmailCodeService.checkEmailCode(email, emailCode);
        SysUser updateUser = new SysUser();
        updateUser.setEmail(email);
        updateUser.setPassword(DigestUtils.md5Hex(password));
        sysUserMapper.updateByEmail(updateUser);
    }

    @Override
    public int updateByUserId(SysUser updateUser) {
        return sysUserMapper.updateById(updateUser, updateUser.getId());
    }

    @Override
    public SessionLoginUserDto qqLogin(String code) {
        // 通过回调code获取access token
        String accessToken = QQLoginApi.getAccessToken(code, qqLoginConfig);

        // 获取openid
        String openId = QQLoginApi.getOpenId(accessToken, qqLoginConfig);

        String avatar = null;
        // 根据qq openId查询用户
        SysUser sysUser = sysUserMapper.selectByQqOpenId(openId);
        if (null == sysUser) {
            // 获取QQ用户信息
            QQUserDto qqUserDto = QQLoginApi.getUserInfo(accessToken, openId, qqLoginConfig);

            // 第一次QQ登录，自动注册用户信息
            sysUser = new SysUser();
            String nickName = qqUserDto.getNickname();
            nickName = nickName.length() > SystemConstants.LENGTH_20 ? nickName.substring(0, SystemConstants.LENGTH_20) : nickName;
            avatar = StringUtils.isEmpty(qqUserDto.getFigureurl_qq_2()) ? qqUserDto.getFigureurl_qq_1() : qqUserDto.getFigureurl_qq_2();

            String userId = RandomStringUtils.random(SystemConstants.LENGTH_10, false, true);
            sysUser.setId(userId);
            sysUser.setNickName(nickName);
            sysUser.setQqOpenId(openId);
            sysUser.setQqAvatar(avatar);
            sysUser.setCreateTime(new Date());
            sysUser.setUsedSpace(0L);
            sysUser.setTotalSpace(systemCache.getSystemSettings().getInitUserSpace() * SystemConstants.MB);
            sysUserMapper.insert(sysUser);
        } else {
            SysUser updateUser = new SysUser();
            updateUser.setQqOpenId(openId);
            updateUser.setLastLoginTime(new Date());
            sysUserMapper.updateByQqOpenId(updateUser);

            avatar = sysUser.getQqAvatar();
        }

        String userId = sysUser.getId();

        SessionLoginUserDto loginUserDto = new SessionLoginUserDto();
        loginUserDto.setUserId(userId);
        loginUserDto.setNickName(sysUser.getNickName());
        loginUserDto.setAvatar(avatar);
        // 判断是否是管理员邮箱
        if (sysUser.getRole() == 1) {
            loginUserDto.setIsAdmin(true);
        } else {
            loginUserDto.setIsAdmin(false);
        }

        // 查询用户已使用空间
        Long usedSpace = sysFileInfoMapper.selectUsedSpace(userId);

        // 保存用户空间信息到redis缓存
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUsedSpace(usedSpace);
        userSpaceDto.setTotalSpace(sysUser.getTotalSpace());
        systemCache.saveUserSpace(userId, userSpaceDto);

        return loginUserDto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserStatus(String userId, Integer status) {
        SysUser updateUser = new SysUser();
        updateUser.setStatus(status);
        if (UserStatusEnum.DISABLE.getStatus() == status) {
            updateUser.setUsedSpace(0L);

            // 删除用户所有文件
            sysFileInfoMapper.deleteByUserId(userId);
        }
        sysUserMapper.updateById(updateUser, userId);
    }

    @Override
    public void changeUserSpace(String userId, Integer changeSpace) {
        Long space = changeSpace * SystemConstants.MB;
        // 更新用户网盘空间
        sysUserMapper.updateUserSpace(userId, null, space);

        // 重置用户空间信息
        systemCache.resetUserSpace(userId);
    }

    @Override
    public SysUser selectByUserId(String userId) {
        return sysUserMapper.selectById(userId);
    }
}




