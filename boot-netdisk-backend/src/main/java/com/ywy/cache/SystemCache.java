package com.ywy.cache;

import com.ywy.core.config.SystemConfig;
import com.ywy.core.constants.SystemConstants;
import com.ywy.mapper.SysFileInfoMapper;
import com.ywy.mapper.SysUserMapper;
import com.ywy.pojo.dto.DownloadFileDto;
import com.ywy.pojo.dto.SystemSettingsDto;
import com.ywy.pojo.dto.UserSpaceDto;
import com.ywy.pojo.entity.SysUser;
import com.ywy.util.RedisUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * redis cache operation
 */
@Component
public class SystemCache {
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private SysFileInfoMapper sysFileInfoMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * Get system setting information
     * @return
     */
    public SystemSettingsDto getSystemSettings() {
        SystemSettingsDto systemSettingsDto = (SystemSettingsDto) redisUtil.get(SystemConstants.REDIS_KEY_SYSTEM_SETTINGS);
        if (systemSettingsDto == null) {
            systemSettingsDto = new SystemSettingsDto();
            systemSettingsDto.setRegisterEmailTitle(systemConfig.getRegisterEmailTitle());
            systemSettingsDto.setRegisterEmailContent(systemConfig.getRegisterEmailContent());
            systemSettingsDto.setInitUserSpace(systemConfig.getInitUserSpace());
            redisUtil.set(SystemConstants.REDIS_KEY_SYSTEM_SETTINGS, systemSettingsDto);
        }
        return systemSettingsDto;
    }

    /**
     * 保存系统设置信息
     * @param systemSettingsDto
     */
    public void saveSystemSettings(SystemSettingsDto systemSettingsDto) {
        redisUtil.set(SystemConstants.REDIS_KEY_SYSTEM_SETTINGS, systemSettingsDto);
    }

    /**
     * 保存用户空间信息
     * @param userId
     * @param userSpaceDto
     */
    public void saveUserSpace(String userId, UserSpaceDto userSpaceDto) {
        redisUtil.setex(SystemConstants.REDIS_KEY_USER_SPACE + userId, userSpaceDto, SystemConstants.REDIS_KEY_EXPIRES_DAY);
    }
    /**
     * 获取用户空间信息
     * @param userId
     * @return
     */
    public UserSpaceDto getUserSpace(String userId) {
        UserSpaceDto userSpaceDto = (UserSpaceDto) redisUtil.get(SystemConstants.REDIS_KEY_USER_SPACE + userId);
        if (null == userSpaceDto) {
            userSpaceDto = new UserSpaceDto();
            // 查询用户已使用空间（用户已经上传文件大小总和）
            Long usedSpace = sysFileInfoMapper.selectUsedSpace(userId);
            userSpaceDto.setUsedSpace(usedSpace);
            userSpaceDto.setTotalSpace(getSystemSettings().getInitUserSpace() * SystemConstants.MB);
            saveUserSpace(userId, userSpaceDto);
        }
        return userSpaceDto;
    }

    /**
     * 重置用户空间信息
     * @param userId
     */
    public void resetUserSpace(String userId) {
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        // 查询用户已使用空间（用户已经上传文件大小总和）
        Long usedSpace = sysFileInfoMapper.selectUsedSpace(userId);
        userSpaceDto.setUsedSpace(usedSpace);
        // 查询用户信息
        SysUser sysUser = sysUserMapper.selectById(userId);
        userSpaceDto.setTotalSpace(sysUser.getTotalSpace());
        saveUserSpace(userId, userSpaceDto);
    }

    /**
     * 增加用户临时文件大小
     * @param userId
     * @param fileId
     * @param fileSize
     */
    public void addFileTempSize(String userId, String fileId, Long fileSize) {
        Long currentSize = getFileTempSize(userId, fileId);
        redisUtil.setex(SystemConstants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId, currentSize + fileSize, SystemConstants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }
    /**
     * 获取用户临时文件大小
     * @param userId
     * @param fileId
     * @return
     */
    public Long getFileTempSize(String userId, String fileId) {
        Object fileTempSize = redisUtil.get(SystemConstants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
        if (fileTempSize == null) {
            return 0L;
        }
        if (fileTempSize instanceof Integer) {
            return ((Integer)fileTempSize).longValue();
        } else if (fileTempSize instanceof Long) {
            return (Long) fileTempSize;
        }
        return 0L;
    }

    /**
     * 保存下载码
     * @param code
     * @param downloadFileDto
     */
    public void saveDownloadCode(String code, DownloadFileDto downloadFileDto) {
        redisUtil.setex(SystemConstants.REDIS_KEY_DOWNLOAD_CODE + code, downloadFileDto, SystemConstants.REDIS_KEY_EXPIRES_FIVE_MIN);
    }

    /**
     * 获取下载码
     * @param code
     * @return
     */
    public DownloadFileDto getDownloadCode(String code) {
        return (DownloadFileDto) redisUtil.get(SystemConstants.REDIS_KEY_DOWNLOAD_CODE + code);
    }
}
