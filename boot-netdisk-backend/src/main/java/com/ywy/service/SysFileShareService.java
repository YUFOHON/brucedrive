package com.ywy.service;

import com.ywy.core.base.BaseService;
import com.ywy.pojo.dto.SessionShareDto;
import com.ywy.pojo.entity.SysFileShare;
import com.ywy.pojo.param.FileShareParam;

/**
* @author ywy
* @description 针对表【sys_file_share(文件分享)】的数据库操作Service
* @createDate 2023-10-31 17:42:12
*/
public interface SysFileShareService extends BaseService<SysFileShare, FileShareParam> {
    /**
     * 分享文件
     * @param fileId
     * @param userId
     * @param validType
     * @param code
     * @return
     */
    SysFileShare shareFile(String fileId, String userId, Integer validType, String code);

    /**
     * 取消分享
     * @param shareIds
     * @param userId
     */
    void cancelShare(String shareIds, String userId);

    /**
     * 根据风险ID查询
     * @param shareId
     * @return
     */
    SysFileShare selectByShareId(String shareId);

    /**
     * 校验提取码
     * @param shareId
     * @param code
     * @return
     */
    SessionShareDto checkShareCode(String shareId, String code);
}
