package com.ywy.mapper;

import com.ywy.core.base.BaseMapper;
import com.ywy.pojo.entity.SysFileInfo;
import com.ywy.pojo.param.FileInfoParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author BRUCE
 * @description Database operation Mapper for table [sys_file_info (file information table)]
 * @createDate 2024-6-9 16:36:14
 * @Entity com.ywy.pojo.com.ywy.pojo.entity.SysFileInfo
 */
public interface SysFileInfoMapper extends BaseMapper<SysFileInfo, FileInfoParam> {
    /**
     * 查询用户已使用空间
     *
     * @param userId
     * @return
     */
    Long selectUsedSpace(String userId);

    /**
     * 根据文件id和用户id查询文件信息
     *
     * @param fileId
     * @param userId
     * @return
     */
    SysFileInfo selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

    /**
     * 修改文件状态
     *
     * @param fileId
     * @param userId
     * @param bean
     * @param oldStatus
     * @return
     */
    int updateFileStatus(@Param("fileId") String fileId,
                         @Param("userId") String userId,
                         @Param("bean") SysFileInfo bean,
                         @Param("oldStatus") Integer oldStatus);

    /**
     * 根据文件id和用户id更新文件信息
     *
     * @param fileId
     * @param userId
     * @param bean
     * @return
     */
    int updateByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId, @Param("bean") SysFileInfo bean);

    /**
     * 批量修改文件
     * @param fileInfo
     * @param userId
     * @param filePidList
     * @param fileIdList
     * @param oldDelFlag
     * @return
     */
    int updateByFileIdBatch(@Param("bean") SysFileInfo fileInfo, @Param("userId") String userId,
                            @Param("filePidList") List<String> filePidList, @Param("fileIdList") List<String> fileIdList,
                            @Param("oldDelFlag") Integer oldDelFlag);

    /**
     * 批量删除文件
     * @param userId
     * @param filePidList
     * @param fileIdList
     * @param oldDelFlag
     * @return
     */
    int deleteByFileIdBatch(@Param("userId") String userId, @Param("filePidList") List<String> filePidList,
                             @Param("fileIdList") List<String> fileIdList, @Param("oldDelFlag") Integer oldDelFlag);

    /**
     * 根据用户ID删除文件
     * @param userId
     * @return
     */
    int deleteByUserId(@Param("userId") String userId);
}




