package com.ywy.controller;

import com.ywy.core.annotation.GlobalInterceptor;
import com.ywy.core.annotation.Valid;
import com.ywy.core.base.BaseController;
import com.ywy.pojo.entity.SysFileInfo;
import com.ywy.pojo.enums.FileCategoryEnum;
import com.ywy.pojo.enums.FileClassEnum;
import com.ywy.pojo.enums.FileDelFlagEnum;
import com.ywy.core.vo.BaseResponseVO;
import com.ywy.core.vo.PageResultVO;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.dto.UploadResultDto;
import com.ywy.pojo.param.FileInfoParam;
import com.ywy.pojo.vo.FileInfoVO;
import com.ywy.pojo.vo.FolderVO;
import com.ywy.service.SysFileInfoService;
import com.ywy.util.BeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 文件相关
 */
@RestController
@RequestMapping("file")
public class FileController extends BaseController {
    @Resource
    private SysFileInfoService sysFileInfoService;

    /**
     * 分頁查詢文件列表頁
     * @param session
     * @param param
     * @param category
     * @return
     */
    @GlobalInterceptor
    @RequestMapping("loadDataList")
    public BaseResponseVO loadDataList(HttpSession session, FileInfoParam param, String category) {
        if (StringUtils.isNotEmpty(category)) {
            FileCategoryEnum categoryEnum = FileCategoryEnum.getByCode(category);
            if (categoryEnum != null) {
                param.setFileCategory(categoryEnum.getCategory());
            }
        }
        param.setUserId(getUserInfoFromSession(session).getUserId());
        param.setDelFlag(FileDelFlagEnum.USING.getFlag());
        param.setOrderBy("update_time desc");
        PageResultVO pageResultVO = sysFileInfoService.findListByPage(param);
        return successResponse(covertPageResultVO(pageResultVO, FileInfoVO.class));
    }

    /**
     * 上傳文件（秒傳、分片）
     * @param session
     * @param file
     * @param fileId
     * @param fileName
     * @param filePid
     * @param fileMd5
     * @param chunkIndex
     * @param chunks
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("uploadFile")
    public BaseResponseVO uploadFile(HttpSession session, MultipartFile file, String fileId,
                                     @Valid(required = true) String fileName,
                                     @Valid(required = true) String filePid,
                                     @Valid(required = true) String fileMd5,
                                     @Valid(required = true) Integer chunkIndex,
                                     @Valid(required = true) Integer chunks) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        UploadResultDto uploadResultDto = sysFileInfoService.uploadFile(loginUserDto, file, fileId, fileName, filePid, fileMd5, chunkIndex, chunks);
        return successResponse(uploadResultDto);
    }

    /**
     * 取得封面圖片
     * @param response
     * @param imageFolder
     * @param imageName
     */
    @RequestMapping("getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName) {
        sysFileInfoService.getImage(response, imageFolder, imageName);
    }

    /**
     * 讀取文件（文字、文件、PDF等）
     * @param response
     * @param session
     * @param fileId
     */
    @GlobalInterceptor
    @RequestMapping("getFile/{fileId}")
    public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        sysFileInfoService.getFile(response, fileId, loginUserDto.getUserId());
    }

    /**
     * 讀取視訊檔案（m3u8索引檔案或視訊分片檔案）
     * @param response
     * @param session
     * @param fileId
     */
    @GlobalInterceptor
    @RequestMapping("getVideoFile/{fileId}")
    public void getVideoFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        sysFileInfoService.getFile(response, fileId, loginUserDto.getUserId());
    }

    /**
     * 新目錄
     * @param session
     * @param filePid
     * @param fileName
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("newFolder")
    public BaseResponseVO newFolder(HttpSession session, @Valid(required = true) String filePid, @Valid(required = true) String fileName) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        SysFileInfo fileInfo = sysFileInfoService.newFolder(filePid, fileName, loginUserDto.getUserId());
        return successResponse(BeanUtil.copy(fileInfo, FileInfoVO.class));
    }

    /**
     * 根據路徑取得目錄列表
     * @param session
     * @param path
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("getFolderList")
    public BaseResponseVO getFolderList(HttpSession session, @Valid(required = true) String path) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        List<SysFileInfo> folderList = sysFileInfoService.getFolderList(path, loginUserDto.getUserId());
        return successResponse(BeanUtil.copyList(folderList, FolderVO.class));
    }

    /**
     * 文件重命名
     * @param session
     * @param fileId
     * @param fileName
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("rename")
    public BaseResponseVO rename(HttpSession session, @Valid(required = true) String fileId, @Valid(required = true) String fileName) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        SysFileInfo fileInfo = sysFileInfoService.rename(fileId, fileName, loginUserDto.getUserId());
        return successResponse(BeanUtil.copy(fileInfo, FileInfoVO.class));
    }

    /**
     * 取得目錄列表
     * @param session
     * @param filePid
     * @param currFileIds
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("loadFolderList")
    public BaseResponseVO loadFolderList(HttpSession session, @Valid(required = true) String filePid, String currFileIds) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);

        FileInfoParam param = new FileInfoParam();
        param.setUserId(loginUserDto.getUserId());
        // select file list by file parent id (pid)
        param.setFilePid(filePid);
        // exclude the current file ids
        if (StringUtils.isNotEmpty(currFileIds)) {
            param.setFileIdArrNotIn(currFileIds.split(","));
        }
        param.setFileClass(FileClassEnum.FOLDER.getType());
        param.setDelFlag(FileDelFlagEnum.USING.getFlag());
        param.setOrderBy("create_time desc");
        List<SysFileInfo> fileInfoList = sysFileInfoService.findListByParam(param);
        return successResponse(BeanUtil.copyList(fileInfoList, FileInfoVO.class));
    }

    /**
     * 移動檔案
     * @param session
     * @param fileIds
     * @param filePid
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("moveFile")
    public BaseResponseVO moveFile(HttpSession session, @Valid(required = true) String fileIds, @Valid(required = true) String filePid) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        sysFileInfoService.changeFolder(fileIds, filePid, loginUserDto.getUserId());
        return successResponse(null);
    }

    /**
     * 產生下載碼
     * @param session
     * @param fileId
     * @return
     */
    @GlobalInterceptor
    @RequestMapping("createDownloadCode/{fileId}")
    public BaseResponseVO createDownloadCode(HttpSession session, @PathVariable("fileId") String fileId) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        String downloadCode = sysFileInfoService.createDownloadCode(fileId, loginUserDto.getUserId());
        return successResponse(downloadCode);
    }

    /**
     * 下載文件
     * @param request
     * @param response
     * @param code
     */
    @GlobalInterceptor(login = false)
    @RequestMapping("downloadFile/{code}")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, @PathVariable("code") String code) {
        sysFileInfoService.downloadFile(request, response, code);
    }

    /**
     * 刪除檔案到回收站
     * @param session
     * @param fileIds
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("deleteFile")
    public BaseResponseVO deleteFile(HttpSession session, @Valid(required = true) String fileIds) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        sysFileInfoService.removeFile2Recycle(fileIds, loginUserDto.getUserId());
        return successResponse(null);
    }
}
