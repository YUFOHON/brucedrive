package com.ywy.service.impl;

import com.ywy.core.base.BaseServiceImpl;
import com.ywy.cache.SystemCache;
import com.ywy.core.constants.SystemConstants;
import com.ywy.core.config.SystemConfig;
import com.ywy.core.enums.ResponseCodeEnum;
import com.ywy.core.exception.BusinessException;
import com.ywy.mapper.SysFileInfoMapper;
import com.ywy.mapper.SysUserMapper;
import com.ywy.pojo.dto.DownloadFileDto;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.dto.UploadResultDto;
import com.ywy.pojo.dto.UserSpaceDto;
import com.ywy.pojo.entity.SysFileInfo;
import com.ywy.pojo.entity.SysUser;
import com.ywy.pojo.enums.*;
import com.ywy.pojo.param.FileInfoParam;
import com.ywy.service.SysFileInfoService;
import com.ywy.util.FFMpegUtil;
import com.ywy.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author ywy
* @description 针对表【sys_file_info(文件信息表)】的数据库操作Service实现
* @createDate 2023-10-24 16:36:14
*/
@Slf4j
@Service
public class SysFileInfoServiceImpl extends BaseServiceImpl<SysFileInfo, FileInfoParam> implements SysFileInfoService {
    @Resource
    private SysFileInfoMapper sysFileInfoMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SystemCache systemCache;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    @Lazy
    private SysFileInfoServiceImpl sysFileInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UploadResultDto uploadFile(SessionLoginUserDto loginUserDto, MultipartFile file, String fileId, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
        UploadResultDto resultDto = new UploadResultDto();
        Boolean uploadSuccess = true;
        File tempFileFolderFile = null;
        try {
            if (StringUtils.isEmpty(fileId)) {
                fileId = RandomStringUtils.random(SystemConstants.LENGTH_10, true, true);
            }
            resultDto.setFileId(fileId);

            String userId = loginUserDto.getUserId();
            UserSpaceDto userSpaceDto = systemCache.getUserSpace(userId);

            // First chunk
            if (chunkIndex == 0) {
                // 查询文件是否已存在
                FileInfoParam param = new FileInfoParam();
                param.setPageNo(1);
                param.setPageSize(1);
                param.setFileMd5(fileMd5);
                param.setStatus(FileStatusEnum.USING.getStatus());
                List<SysFileInfo> fileInfoList = sysFileInfoMapper.selectList(param);
                // 如果文件已存在则秒传
                if (!fileInfoList.isEmpty()) {
                    SysFileInfo fileInfo = fileInfoList.get(0);
                    // 判断网盘空间是否足够
                    if (fileInfo.getFileSize() + userSpaceDto.getUsedSpace() > userSpaceDto.getTotalSpace()) {
                        throw new BusinessException(ResponseCodeEnum.CODE_901);
                    }

                    fileInfo.setId(fileId);
                    fileInfo.setUserId(userId);
                    fileInfo.setFilePid(filePid);
                    fileInfo.setStatus(FileStatusEnum.USING.getStatus());
                    fileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
                    fileInfo.setFileMd5(fileMd5);

                    // 文件重命名
                    fileName = autoRename(userId, filePid, fileName);

                    fileInfo.setFileName(fileName);
                    sysFileInfoMapper.insert(fileInfo);

                    // 更新用户使用空间
                    updateUserSpace(userId, fileInfo.getFileSize());
                    resultDto.setStatus(UploadStatusEnum.UPLOAD_SECONDS.getCode());

                    return resultDto;
                }
            }

            // 判斷網盤空間是否足夠
            Long currentFileTempSize = systemCache.getFileTempSize(userId, fileId);
            if (file.getSize() + currentFileTempSize + userSpaceDto.getUsedSpace() > userSpaceDto.getTotalSpace()) {
                throw new BusinessException(ResponseCodeEnum.CODE_901);
            }

            // 暫存暫存目錄
            String tempFolder = systemConfig.getRootPath() + SystemConstants.FILE_TEMP_FOLDER;
            String currentUserFolder = userId + fileId;
            tempFileFolderFile = new File(tempFolder + currentUserFolder);
            if (!tempFileFolderFile.exists()) {
                tempFileFolderFile.mkdirs();
            }

            // 儲存目前文件分片
            File newFile = new File(tempFileFolderFile.getPath() + "/" + chunkIndex);
            file.transferTo(newFile);

            // 儲存臨時檔案大小
            systemCache.addFileTempSize(userId, fileId, file.getSize());

            // 文件上傳未結束
            if (chunkIndex < chunks - 1) {
                resultDto.setStatus(UploadStatusEnum.UPLOADING.getCode());
                return resultDto;
            }

            // 最後一個分片上傳完成，記錄資料庫，異步合併chunk
            String month = DateFormatUtils.format(new Date(), "yyyyMM");
            String fileSuffix = FileUtil.getFileSuffix(fileName);
            String realFileName = currentUserFolder + fileSuffix; // REAL FILE NAME
            FileTypeEnum fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
            fileName = autoRename(filePid, userId, fileName);

            //Save file name
            SysFileInfo saveFileInfo = new SysFileInfo();
            saveFileInfo.setId(fileId);
            saveFileInfo.setUserId(userId);
            saveFileInfo.setFileMd5(fileMd5);
            saveFileInfo.setFilePid(filePid);
            saveFileInfo.setFileName(fileName);
            saveFileInfo.setFilePath(month + "/" + realFileName);
            saveFileInfo.setFileClass(FileClassEnum.FILE.getType());
            saveFileInfo.setFileCategory(fileTypeEnum.getCategory().getCategory());
            saveFileInfo.setFileType(fileTypeEnum.getType());
            saveFileInfo.setStatus(FileStatusEnum.TRANSFER.getStatus());
            saveFileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
            sysFileInfoMapper.insert(saveFileInfo);

            // 更新用户空间大小
            Long fileTempSize = systemCache.getFileTempSize(userId, fileId);
            updateUserSpace(userId, fileTempSize);

            resultDto.setStatus(UploadStatusEnum.UPLOAD_FINISH.getCode());

            // Asynchronous file transcoding
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sysFileInfoService.transferFile(saveFileInfo.getId(), userId);
                }
            });
        } catch (BusinessException e) {
            log.error("文件上傳失敗", e);
            uploadSuccess = false;
            throw e;
        } catch (IOException e) {
            log.error("文件上傳失敗", e);
            uploadSuccess = false;
        } finally {
            if (!uploadSuccess && tempFileFolderFile != null) {
                try {
                    FileUtils.deleteDirectory(tempFileFolderFile);
                } catch (IOException e) {
                    log.error("刪除暫存目錄失敗", e);
                }
            }
        }

        return resultDto;
    }

    @Override
    public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (StringUtils.isEmpty(imageFolder) || StringUtils.isEmpty(imageName) || !FileUtil.checkPath(imageFolder) || !FileUtil.checkPath(imageName)) {
            return;
        }
        String imageSuffix = FileUtil.getFileSuffix(imageName);
        String filePath = systemConfig.getRootPath() + SystemConstants.FILE_FOLDER + imageFolder + "/" + imageName;
        imageSuffix = imageSuffix.replace(".", "");
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);
        FileUtil.readFile(response, filePath);
    }

    @Override
    public void getFile(HttpServletResponse response, String fileId, String userId) {
        String filePath = null;

        // 视频分片文件
        if (fileId.endsWith(".ts")) {
            String[] tsArr = fileId.split("_");
            String realFileId = tsArr[0];
            SysFileInfo fileInfo = sysFileInfoMapper.selectByFileIdAndUserId(realFileId, userId);
            if (null == fileInfo) {
                return;
            }

            String fileName = fileInfo.getFilePath();
            fileName = FileUtil.getFileNameNoSuffix(fileName) + "/" + fileId;
            filePath = systemConfig.getRootPath() + SystemConstants.FILE_FOLDER + fileName;
        } else {
            SysFileInfo fileInfo = sysFileInfoMapper.selectByFileIdAndUserId(fileId, userId);
            if (null == fileInfo) {
                return;
            }

            // Read the m3u8 index file that generate from ffmpeg
            if (FileCategoryEnum.VIDEO.getCategory() == fileInfo.getFileCategory()) {
                String fileNameNoSuffix = FileUtil.getFileNameNoSuffix(fileInfo.getFilePath());
                filePath = systemConfig.getRootPath() + SystemConstants.FILE_FOLDER + fileNameNoSuffix + "/" + SystemConstants.M3U8_NAME;
            } else {
                // for other files (text, pdf, etc.)
                filePath = systemConfig.getRootPath() + SystemConstants.FILE_FOLDER + fileInfo.getFilePath();
            }
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        FileUtil.readFile(response, filePath);
    }

    @Override
    public SysFileInfo newFolder(String filePid, String folderName, String userId) {
        // 检验文件名
        checkFileName(filePid, userId, folderName, FileClassEnum.FOLDER.getType(), 0);

        // 保存文件信息
        SysFileInfo saveFileInfo = new SysFileInfo();
        saveFileInfo.setId(RandomStringUtils.random(SystemConstants.LENGTH_10, true, true));
        saveFileInfo.setUserId(userId);
        saveFileInfo.setFilePid(filePid);
        saveFileInfo.setFileName(folderName);
        saveFileInfo.setFileClass(FileClassEnum.FOLDER.getType());
        saveFileInfo.setStatus(FileStatusEnum.USING.getStatus());
        saveFileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());

        Date curDate = new Date();
        saveFileInfo.setCreateTime(curDate);
        saveFileInfo.setUpdateTime(curDate);

        sysFileInfoMapper.insert(saveFileInfo);

        return saveFileInfo;
    }
    /**
     * Retrieves a list of folders based on the given path and user ID.
     * for navigation bar
     *
     * @param path    The file path to retrieve the folder list for.
     * @param userId  The ID of the user to filter the folder list by.
     * @return        A list of SysFileInfo objects representing the folders.
     */
    @Override
    public List<SysFileInfo> getFolderList(String path, String userId) {
        String[] pathArr = path.split("/");
        FileInfoParam param = new FileInfoParam();
        param.setUserId(userId);
        param.setFileClass(FileClassEnum.FOLDER.getType());
        param.setFileIdArrIn(pathArr);
        String orderBy = "field(id,\"" + StringUtils.join(pathArr, "\",\"") + "\")";
        param.setOrderBy(orderBy);
        return sysFileInfoMapper.selectList(param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysFileInfo rename(String fileId, String fileName, String userId) {
        SysFileInfo fileInfo = sysFileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (null == fileInfo) {
            throw new BusinessException("file doesn't exist");
        }

        String filePid = fileInfo.getFilePid();
        // check file name for duplicates
        checkFileName(filePid, userId, fileName, fileInfo.getFileClass(), 0);
        // file cannot modified to its suffox
        if (FileClassEnum.FILE.getType() == fileInfo.getFileClass()) {
            fileName = fileName + FileUtil.getFileSuffix(fileInfo.getFileName());
        }

        // modify file name
        Date curDate = new Date();
        SysFileInfo updateFileInfo = new SysFileInfo();
        updateFileInfo.setFileName(fileName);
        updateFileInfo.setUpdateTime(curDate);
        sysFileInfoMapper.updateByFileIdAndUserId(fileId, userId, updateFileInfo);

        // 再次查询文件是否重名
        checkFileName(filePid, userId, fileName, fileInfo.getFileClass(), 1);

        fileInfo.setFileName(fileName);
        fileInfo.setUpdateTime(curDate);
        return fileInfo;
    }

    @Override
    public void changeFolder(String fileIds, String filePid, String userId) {
        // 文件不能移动到其自身下
        if (fileIds.contains(filePid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_900);
        }
        if (!SystemConstants.ZERO_STR.equals(filePid)) {
            // 查询父目录是否存在
            SysFileInfo fileInfo = sysFileInfoMapper.selectByFileIdAndUserId(filePid, userId);
            if (fileInfo == null || FileDelFlagEnum.USING.getFlag() != fileInfo.getDelFlag()) {
                throw new BusinessException(ResponseCodeEnum.CODE_601);
            }
        }

        // search the target directory file list -- all the files under that directory
        FileInfoParam param = new FileInfoParam();
        param.setFilePid(filePid);
        param.setUserId(userId);
        List<SysFileInfo> targetFileInfoList = sysFileInfoMapper.selectList(param);
        // 轉換為檔案名稱為key的map
        Map<String, SysFileInfo> targetFileMap = targetFileInfoList.stream().collect(Collectors.toMap(SysFileInfo::getFileName, Function.identity(), (file1, file2) -> file2));

        //search files i want to move base on the file id
//            <if test="param.fileName != null and param.fileName!=''">
//                and file_name = #{param.fileName}
//            </if>
        String[] fileIdArr = fileIds.split(",");
        // 查詢要移動的文件
        param = new FileInfoParam();
        param.setUserId(userId);
        param.setFileIdArrIn(fileIdArr);
        List<SysFileInfo> fileInfoList = sysFileInfoMapper.selectList(param);

        // File Renaming if the target directory contains a file with the same name
        for (SysFileInfo item : fileInfoList) {
            SysFileInfo updateFileInfo = new SysFileInfo();
            // Determine if there is a file with the same name, and rename it if it exists
            SysFileInfo targetFileInfo = targetFileMap.get(item.getFileName());
            if (targetFileInfo != null) {
                String fileName = FileUtil.rename(item.getFileName());
                updateFileInfo.setFileName(fileName);
            }
            //set the file parent id to new parent id
            updateFileInfo.setFilePid(filePid);
            sysFileInfoMapper.updateByFileIdAndUserId(item.getId(), userId, updateFileInfo);
        }
    }

    @Override
    public String createDownloadCode(String fileId, String userId) {
        SysFileInfo fileInfo = sysFileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }

        // can not download folder
        if (FileClassEnum.FOLDER.getType() == fileInfo.getFileClass()) {
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }

        String code = RandomStringUtils.random(SystemConstants.LENGTH_50, true, true);
        DownloadFileDto fileDto = new DownloadFileDto();
        fileDto.setCode(code);
        fileDto.setFileName(fileInfo.getFileName());
        fileDto.setFilePath(fileInfo.getFilePath());
        systemCache.saveDownloadCode(code, fileDto);

        return code;
    }

    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, String code)  {
        DownloadFileDto downloadFileDto = systemCache.getDownloadCode(code);
        if (downloadFileDto  == null) {
            return;
        }

        try {
            String filePath = systemConfig.getRootPath() + SystemConstants.FILE_FOLDER + downloadFileDto.getFilePath();
            String fileName = downloadFileDto.getFileName();

            response.setContentType("application/x-msdownload;charset=UTF-8");
            // IE
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            }
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            FileUtil.readFile(response, filePath);
        } catch (Exception e) {
            log.error("Download file fail", e);
        }
    }

    @Override
    public void removeFile2Recycle(String fileIds, String userId) {
        // 查询待删除的文件
        String[] fileIdArr = fileIds.split(",");
        FileInfoParam param = new FileInfoParam();
        param.setUserId(userId);
        param.setFileIdArrIn(fileIdArr);
        param.setDelFlag(FileDelFlagEnum.USING.getFlag());
        List<SysFileInfo> fileInfoList = sysFileInfoMapper.selectList(param);
        if (fileInfoList.isEmpty()) {
            return;
        }

        // recursively query all children of the file to be deleted
        List<String> delFilePidList = new ArrayList<>();
        for (SysFileInfo fileInfo : fileInfoList) {
            if (Objects.equals(FileClassEnum.FOLDER.getType(), fileInfo.getFileClass())) {
                findAllFileList(delFilePidList, fileInfo.getId(), userId, FileDelFlagEnum.USING.getFlag());
            }
        }

        // change the status of all children of the file to be deleted to DEL
        if (!delFilePidList.isEmpty()) {
            SysFileInfo updateFileInfo = new SysFileInfo();
            updateFileInfo.setDelFlag(FileDelFlagEnum.DEL.getFlag());
            sysFileInfoMapper.updateByFileIdBatch(updateFileInfo, userId, delFilePidList, null, FileDelFlagEnum.USING.getFlag());
        }

        // change  the status of the file to be deleted to DEL
        List<String> delFileIdList = Arrays.asList(fileIdArr);
        SysFileInfo updateFileInfo = new SysFileInfo();
        updateFileInfo.setRecycleTime(new Date());
        updateFileInfo.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        sysFileInfoMapper.updateByFileIdBatch(updateFileInfo, userId, null, delFileIdList, FileDelFlagEnum.USING.getFlag());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertFile(String fileIds, String userId) {
        // Query the files to be restored
        String[] fileIdArr = fileIds.split(",");
        FileInfoParam param = new FileInfoParam();
        param.setUserId(userId);
        param.setFileIdArrIn(fileIdArr);
        param.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        List<SysFileInfo> fileInfoList = sysFileInfoMapper.selectList(param);
        if (fileInfoList.isEmpty()) {
            return;
        }

// Recursively query all children of the file to be restored
        List<String> revertFilePidList = new ArrayList<>();
        for (SysFileInfo fileInfo : fileInfoList) {
            if (Objects.equals(fileInfo.getFileClass(), FileClassEnum.FOLDER.getType())) {
                // Find all the children of the file to be restored and it's state is DEL
                findAllFileList(revertFilePidList, fileInfo.getId(), userId, FileDelFlagEnum.DEL.getFlag());
            }
        }

// Query all files in the root directory
        param = new FileInfoParam();
        param.setUserId(userId);
        param.setDelFlag(FileDelFlagEnum.USING.getFlag());
        param.setFilePid(SystemConstants.ZERO_STR);
        List<SysFileInfo> rootFileInfoList = sysFileInfoMapper.selectList(param);

// Convert to a map with file name as key
        Map<String, SysFileInfo> rootFileMap = rootFileInfoList.stream().collect(Collectors.toMap(SysFileInfo::getFileName, Function.identity(), (file1, file2) -> file2));

// Change the status of all children of the file to be restored to normal
        if (!revertFilePidList.isEmpty()) {
            SysFileInfo updateFileInfo = new SysFileInfo();
            updateFileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
            sysFileInfoMapper.updateByFileIdBatch(updateFileInfo, userId, revertFilePidList, null, FileDelFlagEnum.DEL.getFlag());
        }

// Change the status of the file to be restored to normal and move it to the root directory
        List<String> revertFileIdList = Arrays.asList(fileIdArr);
        SysFileInfo updateFileInfo = new SysFileInfo();
        updateFileInfo.setDelFlag(FileDelFlagEnum.USING.getFlag());
        updateFileInfo.setFilePid(SystemConstants.ZERO_STR);
        updateFileInfo.setUpdateTime(new Date());
        sysFileInfoMapper.updateByFileIdBatch(updateFileInfo, userId, null, revertFileIdList, FileDelFlagEnum.RECYCLE.getFlag());

// Rename the file to be restored
        for (SysFileInfo item : fileInfoList) {
// Determine whether there is a file with the same name in the target directory, and rename it if it exists
            SysFileInfo rootFileInfo = rootFileMap.get(item.getFileName());
            if (rootFileInfo != null) {
                String fileName = FileUtil.rename(item.getFileName());
                updateFileInfo = new SysFileInfo();
                updateFileInfo.setFileName(fileName);
                sysFileInfoMapper.updateByFileIdAndUserId(item.getId(), userId, updateFileInfo);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteFile(String fileIds, String userId, Boolean admin) {
        String[] fileIdArr = fileIds.split(",");
        FileInfoParam param = new FileInfoParam();
        param.setUserId(userId);
        param.setFileIdArrIn(fileIdArr);
        // Non-administrators can only delete files in the Recycle Bin
        if (!admin) {
            param.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        }
        List<SysFileInfo> fileInfoList = sysFileInfoMapper.selectList(param);
        if (fileInfoList.isEmpty()) {
            return;
        }

        // Recursively query all children of the file to be deleted
        List<String> delFilePidList = new ArrayList<>();
        for (SysFileInfo fileInfo : fileInfoList) {
            if (FileClassEnum.FOLDER.getType() == fileInfo.getFileClass()) {
                findAllFileList(delFilePidList, fileInfo.getId(), userId, FileDelFlagEnum.DEL.getFlag());
            }
        }

        // Delete all children of the file to be deleted
        // set all the sub files to be DEL state because the recycle bin only contains the files that are deleted
        if (!delFilePidList.isEmpty()) {
            sysFileInfoMapper.deleteByFileIdBatch(userId, delFilePidList, null, admin ? null : FileDelFlagEnum.DEL.getFlag());
        }

        // Delete the file
        // set the user select files to recycle state because this file may be folder or file and
        //if is folder, all the sub files are already set to DEL state and not shown on the recycle bin
        List<String> delFileIdList = Arrays.asList(fileIdArr);
        sysFileInfoMapper.deleteByFileIdBatch(userId, null, delFileIdList, admin ? null : FileDelFlagEnum.RECYCLE.getFlag());
        // Modify user space
        Long usedSpace = sysFileInfoMapper.selectUsedSpace(userId);
        SysUser sysUser = new SysUser();
        sysUser.setUsedSpace(usedSpace);
        sysUserMapper.updateById(sysUser, userId);

        UserSpaceDto userSpaceDto = systemCache.getUserSpace(userId);
        userSpaceDto.setUsedSpace(usedSpace);
        systemCache.saveUserSpace(userId, userSpaceDto);
    }
    /**
     * 查询所有文件及其子级（递归）
     * @param fileIdList
     * @param fileId
     * @param userId
     * @param delFlag
     */
    private void findAllFileList(List<String> fileIdList, String fileId, String userId, Integer delFlag) {
        fileIdList.add(fileId);

        // 查询文件是否有子级
        FileInfoParam param = new FileInfoParam();
        param.setUserId(userId);
        // IF a file's parent id is the same as the file id, then it is a sub file so select it
        param.setFilePid(fileId);
        param.setDelFlag(delFlag);
        param.setFileClass(FileClassEnum.FOLDER.getType());
        List<SysFileInfo> fileInfoList = sysFileInfoMapper.selectList(param);
        // 递归调用，查询所有文件和目录
        for (SysFileInfo fileInfo : fileInfoList) {
            findAllFileList(fileIdList, fileInfo.getId(), userId, delFlag);
        }
    }

    @Override
    public SysFileInfo selectByFileIdAndUserId(String fileId, String userId) {
        return sysFileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    }

    @Override
    public void checkRootFilePid(String rootFilePid, String fileId, String userId) {
        if (StringUtils.isEmpty(fileId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
        if (rootFilePid.equals(fileId)) {
            return;
        }

        checkFilePid(rootFilePid, fileId, userId);
    }

    @Override
    public void saveShare(String shareRootFilePid, String shareFileIds, String targetFolderId, String shareUserId, String currUserId) {
        String[] shareFileIdArr = shareFileIds.split(",");

// Query the list of all files in the target directory
        FileInfoParam param = new FileInfoParam();
        param.setUserId(currUserId);
        param.setFilePid(targetFolderId);
        List<SysFileInfo> targetFileList = sysFileInfoMapper.selectList(param);
        Map<String, SysFileInfo> targetFileMap = targetFileList.stream().collect(Collectors.toMap(SysFileInfo::getFileName, Function.identity(), (file1, file2) -> file2));

// Select the file to be saved
        param = new FileInfoParam();
        param.setUserId(shareUserId);
        param.setFileIdArrIn(shareFileIdArr);
        List<SysFileInfo> shareFileList = sysFileInfoMapper.selectList(param);

        Date currDate = new Date();
// Traverse and get all files to be saved
        List<SysFileInfo> copyFileList = new ArrayList<>();
        for (SysFileInfo fileInfo : shareFileList) {
// Determine whether there is a file with the same name in the target directory, and rename it if it exists
            SysFileInfo haveFile = targetFileMap.get(fileInfo.getFileName());
            if (haveFile != null) {
                fileInfo.setFileName(FileUtil.rename(fileInfo.getFileName()));
            }

            findAllSubFile(copyFileList, fileInfo, shareUserId, currUserId, targetFolderId, currDate);
        }

        if (copyFileList.isEmpty()) {
            return;
        }

// Calculate the file size
        Long totalSize = 0L;
        for (SysFileInfo fileInfo : copyFileList) {
            totalSize += fileInfo.getFileSize();
        }

        UserSpaceDto userSpaceDto = systemCache.getUserSpace(currUserId);
// Determine whether the network disk space is sufficient
        if (totalSize + userSpaceDto.getUsedSpace() > userSpaceDto.getTotalSpace()) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        sysFileInfoMapper.insertBatch(copyFileList);
    }
    /**
     * recursive search for all sub files and folders for save share function
     * @param copyFileList
     * @param sourceFileInfo
     * @param sourceUserId
     * @param currUserId
     * @param newFilePid
     * @param currDate
     */
    private void findAllSubFile(List<SysFileInfo> copyFileList, SysFileInfo sourceFileInfo, String sourceUserId, String currUserId, String newFilePid, Date currDate) {
        sourceFileInfo.setFilePid(newFilePid);
        sourceFileInfo.setUserId(currUserId);
        sourceFileInfo.setCreateTime(currDate);
        String newFileId = RandomStringUtils.random(SystemConstants.LENGTH_10, true, true);
        sourceFileInfo.setId(newFileId);
        copyFileList.add(sourceFileInfo);
        // 如果源文件为目录则递归调用
        if (FileClassEnum.FOLDER.getType() == sourceFileInfo.getFileClass()) {
            FileInfoParam param = new FileInfoParam();
            param.setFilePid(sourceFileInfo.getId());
            param.setUserId(sourceUserId);
            List<SysFileInfo> fileInfoList = sysFileInfoMapper.selectList(param);
            for (SysFileInfo item : fileInfoList) {
                findAllSubFile(copyFileList, item, sourceUserId, currUserId, newFilePid, currDate);
            }
        }
    }

    private void checkFilePid(String rootFilePid, String fileId, String userId) {
        SysFileInfo fileInfo = sysFileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (null == fileInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }

        if (SystemConstants.ZERO_STR.equals(rootFilePid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }

        if (fileInfo.getFilePid().equals(rootFilePid)) {
            return;
        }

        checkFilePid(rootFilePid, fileInfo.getFilePid(), userId);
    }


    /**
     * Verify file name
     * @param filePid
     * @param userId
     * @param fileName
     * @param fileClass
     * @param limitCount Limit the number of repetitions
     */
    private void checkFileName(String filePid, String userId, String fileName, Integer fileClass, Integer limitCount) {
        // 查询文件是否重名
        FileInfoParam param = new FileInfoParam();
        param.setUserId(userId);
        param.setFilePid(filePid);
        param.setFileName(fileName);
        param.setFileClass(fileClass);
        param.setDelFlag(FileDelFlagEnum.USING.getFlag());
        Integer count = sysFileInfoMapper.selectCount(param);
        if (count > limitCount) {
            throw new BusinessException("file name" + fileName + "already exist");
        }
    }

    /**
     * file name auto rename
     * @param userId
     * @param filePid
     * @param fileName
     * @return
     */
    private String autoRename(String userId, String filePid, String fileName) {
        // 查询文件是否重名
        FileInfoParam param = new FileInfoParam();
        param.setUserId(userId);
        param.setFilePid(filePid);
        param.setFileName(fileName);
        param.setDelFlag(FileDelFlagEnum.USING.getFlag());
        Integer count = sysFileInfoMapper.selectCount(param);

        // 存在重名的文件则重新生成文件名
        if (count > 0) {
            fileName = FileUtil.rename(fileName);
        }
        return fileName;
    }

    /**
     * UPDATE USER SPACE
     * @param userId
     * @param fileSize
     */
    private void updateUserSpace(String userId, Long fileSize) {
        // 更新使用者使用空間
        int count = sysUserMapper.updateUserSpace(userId, fileSize, null);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        // 更新redis缓存中的用户空间信息
        UserSpaceDto userSpaceDto = systemCache.getUserSpace(userId);
        userSpaceDto.setUsedSpace(userSpaceDto.getUsedSpace() + fileSize);
        systemCache.saveUserSpace(userId, userSpaceDto);
    }

    /**
     * File Transcoding
     * @param fileId
     * @param userId
     */
    @Async(value = "applicationTaskExecutor")
    public void transferFile(String fileId, String userId) {
        Boolean transferSuccess = true; // 转码是否成功
        String targetFilePath = null;
        String cover = null;
        FileTypeEnum fileTypeEnum = null;
        SysFileInfo fileInfo = sysFileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        try {
            if (fileInfo == null || FileStatusEnum.TRANSFER.getStatus() != fileInfo.getStatus()) {
                return;
            }

            // 临时目录
            String tempFolder = systemConfig.getRootPath() + SystemConstants.FILE_TEMP_FOLDER;
            String currentUserFolder = userId + fileId;
            File tempFileFolderFile = new File(tempFolder + currentUserFolder);

            String month = DateFormatUtils.format(fileInfo.getCreateTime(), "yyyyMM");

            // Target directory, the directory where the file actually resides
            String targetFolder = systemConfig.getRootPath() + SystemConstants.FILE_FOLDER;
            File targetFolderFile = new File(targetFolder + month);
            if (!targetFolderFile.exists()) {
                targetFolderFile.mkdirs();
            }

            // Real file name
            String fileSuffix = FileUtil.getFileSuffix(fileInfo.getFileName());
            String realFileName = currentUserFolder + fileSuffix;
            targetFilePath = targetFolderFile.getPath() + "/" + realFileName;

            // Merge files
            FileUtil.unionFile(tempFileFolderFile.getPath(), targetFilePath, fileInfo.getFileName(), true);

            fileTypeEnum = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
            if (fileTypeEnum == FileTypeEnum.VIDEO) {
                // Video file cutting
                FFMpegUtil.cutFile4Video(fileId, targetFilePath);
                // Generate video cover
                cover = month + "/" + currentUserFolder + SystemConstants.VIDEO_COVER_SUFFIX;
                String coverPath = targetFolder + cover;
                FFMpegUtil.createVideoCover(new File(targetFilePath), SystemConstants.LENGTH_150, new File(coverPath));
            }
            if (fileTypeEnum == FileTypeEnum.IMAGE) {
                // Generate thumbnails
                cover = month + "/" + realFileName.replace(".", "_.");
                String coverPath = targetFolder + cover;
                Boolean created = FFMpegUtil.createThumbnail(new File(targetFilePath), SystemConstants.LENGTH_150, new File(coverPath), false);
                // if the thumbnail is not generated, copy the original file as the cover
                if (!created) {
                    FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }
        } catch (Exception e) {
            log.error("File transcoding failed，fileId：{}，userId：{}",fileId, userId, e);
            transferSuccess = false;
        } finally {
            // 修改文件状态
            SysFileInfo updateFileInfo = new SysFileInfo();
            updateFileInfo.setFileSize(new File(targetFilePath).length());
            updateFileInfo.setFileCover(cover);
            updateFileInfo.setStatus(transferSuccess ? FileStatusEnum.USING.getStatus() : FileStatusEnum.TRANSFER_FILE.getStatus());
            sysFileInfoMapper.updateFileStatus(fileId, userId, updateFileInfo, FileStatusEnum.TRANSFER.getStatus());
        }
    }
}




