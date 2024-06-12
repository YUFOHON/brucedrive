package com.ywy.service.impl;

import com.ywy.core.base.BaseServiceImpl;
import com.ywy.core.constants.SystemConstants;
import com.ywy.core.enums.ResponseCodeEnum;
import com.ywy.core.exception.BusinessException;
import com.ywy.mapper.SysFileShareMapper;
import com.ywy.pojo.dto.SessionShareDto;
import com.ywy.pojo.entity.SysFileShare;
import com.ywy.pojo.enums.ShareValidTypeEnum;
import com.ywy.pojo.param.FileShareParam;
import com.ywy.service.SysFileShareService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author bruce
* @description Implementation of database operation service for table [sys_file_share (file sharing)]
* @createDate 2024-9-6 17:42:12
*/
@Service
public class SysFileShareServiceImpl extends BaseServiceImpl<SysFileShare, FileShareParam> implements SysFileShareService {
    @Resource
    private SysFileShareMapper sysFileShareMapper;

    @Override
    public SysFileShare shareFile(String fileId, String userId, Integer validType, String code) {
        ShareValidTypeEnum typeEnum = ShareValidTypeEnum.getByType(validType);
        if (typeEnum  == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
        Date curDate = new Date();

        SysFileShare fileShare = new SysFileShare();
        fileShare.setId(RandomStringUtils.random(SystemConstants.LENGTH_10, true, true));
        fileShare.setFileId(fileId);
        fileShare.setUserId(userId);
        fileShare.setValidType(validType);
        if (typeEnum  != ShareValidTypeEnum.FOREVER) {
            fileShare.setExpireTime(DateUtils.addDays(curDate, typeEnum.getDays()));
        }
        if (StringUtils.isEmpty(code)) {
            code = RandomStringUtils.random(SystemConstants.LENGTH_5, true, true);
        }
        fileShare.setCode(code);
        fileShare.setShowCount(0);
        fileShare.setCreateTime(curDate);
        sysFileShareMapper.insert(fileShare);

        return fileShare;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelShare(String shareIds, String userId) {
        String[] shareIdArr = shareIds.split(",");
        int count = sysFileShareMapper.deleteBatch(shareIdArr, userId);
        if (count != shareIdArr.length) {
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    @Override
    public SysFileShare selectByShareId(String shareId) {
        return sysFileShareMapper.selectById(shareId);
    }

    @Override
    public SessionShareDto checkShareCode(String shareId, String code) {
        // 分享链接无效或已过期
        SysFileShare fileShare = sysFileShareMapper.selectById(shareId);
        if(fileShare == null || new Date().after(fileShare.getExpireTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }

        if (!fileShare.getCode().equals(code)) {
            throw new BusinessException("Extraction code error");
        }

        // 更新浏览次数
        sysFileShareMapper.updateShowCount(shareId);

        SessionShareDto shareDto = new SessionShareDto();
        shareDto.setShareId(shareId);
        shareDto.setShareUserId(fileShare.getUserId());
        shareDto.setFileId(fileShare.getFileId());
        shareDto.setExpireTime(fileShare.getExpireTime());
        return shareDto;
    }
}




