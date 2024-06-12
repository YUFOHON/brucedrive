package com.ywy.mapper;

import com.ywy.core.base.BaseMapper;
import com.ywy.pojo.entity.SysFileShare;
import com.ywy.pojo.param.FileShareParam;
import org.apache.ibatis.annotations.Param;

/**
* @author ywy
* @description 针对表【sys_file_share(文件分享)】的数据库操作Mapper
* @createDate 2023-10-31 17:42:12
* @Entity .com.ywy.pojo.entity.SysFileShare
*/
public interface SysFileShareMapper extends BaseMapper<SysFileShare, FileShareParam> {
    /**
     * 删除文件分享
     * @param shareIdArr
     * @param userId
     * @return
     */
    int deleteBatch(@Param("shareIdArr") String[] shareIdArr, @Param("userId") String userId);

    /**
     * 更新浏览次数
     * @param shareId
     * @return
     */
    int updateShowCount(String shareId);
}




