package com.ywy.service;

import com.ywy.core.base.BaseService;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.dto.UploadResultDto;
import com.ywy.pojo.entity.SysFileInfo;
import com.ywy.pojo.param.FileInfoParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* @author BRUCE
* @description  sys_file_info service interface
* @createDate 2023-10-24 16:36:14
*/
public interface SysFileInfoService extends BaseService<SysFileInfo, FileInfoParam> {
    /**
     * 上传文件
     * @param loginUserDto
     * @param file
     * @param fileId
     * @param fileName
     * @param filePid
     * @param fileMd5
     * @param chunkIndex
     * @param chunks
     * @return
     */
    UploadResultDto uploadFile(SessionLoginUserDto loginUserDto, MultipartFile file, String fileId, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

    /**
     * 获取图片
     * @param response
     * @param imageFolder
     * @param imageName
     */
    void getImage(HttpServletResponse response, String imageFolder, String imageName);

    /**
     * GET FILE
     * @param response
     * @param fileId
     * @param userId
     */
    void getFile(HttpServletResponse response, String fileId, String userId);

    /**
     * 新建目录
     * @param filePid
     * @param folderName
     * @param userId
     * @return
     */
    SysFileInfo newFolder(String filePid, String folderName, String userId);

    /**
     * 获取目录列表
     * @param path
     * @param userId
     * @return
     */
    List<SysFileInfo> getFolderList(String path, String userId);

    /**
     * 文件重命名
     * @param fileId
     * @param fileName
     * @param userId
     * @return
     */
    SysFileInfo rename(String fileId, String fileName, String userId);

    /**
     * 修改文件所在目录
     * @param fileIds
     * @param filePid
     * @param userId
     */
    void changeFolder(String fileIds, String filePid, String userId);

    /**
     * 生成下载码
     * @param fileId
     * @param userId
     * @return
     */
    String createDownloadCode(String fileId, String userId);

    /**
     *  download file
     * @param request
     * @param response
     * @param code
     */
    void downloadFile(HttpServletRequest request, HttpServletResponse response, String code);

    /**
     * delete file to recycle
     * @param fileIds
     * @param userId
     */
    void removeFile2Recycle(String fileIds, String userId);

    /**
     * 还原文件
     * @param fileIds
     * @param userId
     */
    void revertFile(String fileIds, String userId);

    /**
     * 彻底删除文件
     * @param fileIds
     * @param userId
     * @param admin
     */
    void deleteFile(String fileIds, String userId, Boolean admin);

    /**
     * 根据文件ID和用户ID查询
     * @param fileId
     * @param userId
     * @return
     */
    SysFileInfo selectByFileIdAndUserId(String fileId, String userId);

    /**
     * 校验文件是否属于filePid
     * @param rootFilePid
     * @param fileId
     * @param userId
     */
    void checkRootFilePid(String rootFilePid, String fileId, String userId);



    void saveShare(String shareRootFilePid, String shareFileIds, String targetFolderId, String shareUserId, String currUserId);
}
