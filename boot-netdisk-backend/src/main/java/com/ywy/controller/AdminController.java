package com.ywy.controller;

import com.ywy.cache.SystemCache;
import com.ywy.core.annotation.GlobalInterceptor;
import com.ywy.core.annotation.Valid;
import com.ywy.core.base.BaseController;
import com.ywy.core.vo.BaseResponseVO;
import com.ywy.core.vo.PageResultVO;
import com.ywy.pojo.dto.SystemSettingsDto;
import com.ywy.pojo.entity.SysFileInfo;
import com.ywy.pojo.entity.SysUser;
import com.ywy.pojo.param.FileInfoParam;
import com.ywy.pojo.param.UserParam;
import com.ywy.pojo.vo.FolderVO;
import com.ywy.pojo.vo.UserInfoVO;
import com.ywy.service.SysFileInfoService;
import com.ywy.service.SysUserService;
import com.ywy.util.BeanUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Administrator related, only administrators can operate
 */
@RestController
@RequestMapping("admin")
public class AdminController extends BaseController {
    @Resource
    private SysUserService sysUserService;

    @Resource
    private SysFileInfoService sysFileInfoService;

    @Resource
    private SystemCache systemCache;

    /**
     * 获取系统配置信息
     *
     * @return
     */
    @GlobalInterceptor(admin = true)
    @RequestMapping("getSystemSettings")
    public BaseResponseVO getSystemSettings() {
        return successResponse(systemCache.getSystemSettings());
    }

    /**
     * save system settings
     * @param registerEmailTitle
     * @param registerEmailContent
     * @param initUserSpace
     * @return
     */
    @GlobalInterceptor(admin = true, valid = true)
    @RequestMapping("saveSystemSettings")
    public BaseResponseVO saveSystemSettings(@Valid(required = true) String registerEmailTitle,
                                             @Valid(required = true) String registerEmailContent,
                                             @Valid(required = true) Integer initUserSpace) {
        SystemSettingsDto systemSettingsDto = new SystemSettingsDto();
        systemSettingsDto.setRegisterEmailTitle(registerEmailTitle);
        systemSettingsDto.setRegisterEmailContent(registerEmailContent);
        systemSettingsDto.setInitUserSpace(initUserSpace);
        systemCache.saveSystemSettings(systemSettingsDto);
        return successResponse(null);
    }

    /**
     * 用户列表
     *
     * @param param
     * @return
     */
    @GlobalInterceptor(admin = true)
    @RequestMapping("loadUserList")
    public BaseResponseVO loadUserList(UserParam param) {
        param.setOrderBy("create_time desc");
        PageResultVO<SysUser> pageResultVO = sysUserService.findListByPage(param);
        return successResponse(covertPageResultVO(pageResultVO, UserInfoVO.class));
    }

    /**
     * 修改用户状态
     *
     * @param userId
     * @param status
     * @return
     */
    @GlobalInterceptor(admin = true, valid = true)
    @RequestMapping("updateUserStatus")
    public BaseResponseVO updateUserStatus(@Valid(required = true) String userId, @Valid(required = true) Integer status) {
        sysUserService.updateUserStatus(userId, status);
        return successResponse(null);
    }

    /**
     * 修改用户空间
     *
     * @param userId
     * @param changeSpace
     * @return
     */
    @GlobalInterceptor(admin = true, valid = true)
    @RequestMapping("changeUserSpace")
    public BaseResponseVO changeUserSpace(@Valid(required = true) String userId, @Valid(required = true) Integer changeSpace) {
        sysUserService.changeUserSpace(userId, changeSpace);
        return successResponse(null);
    }

    /**
     * 文件列表
     *
     * @param param
     * @return
     */
    @GlobalInterceptor(admin = true)
    @RequestMapping("loadFileList")
    public BaseResponseVO loadFileList(FileInfoParam param) {
        param.setOrderBy("update_time desc");
        param.setQueryNickName(true);
        PageResultVO pageResultVO = sysFileInfoService.findListByPage(param);
        return successResponse(pageResultVO);
    }

    /**
     * 根据路径获取目录列表
     *
     * @param path
     * @return
     */
    @GlobalInterceptor(admin = true, valid = true)
    @RequestMapping("getFolderList")
    public BaseResponseVO getFolderList(@Valid(required = true) String path) {
        List<SysFileInfo> folderList = sysFileInfoService.getFolderList(path, null);
        return successResponse(BeanUtil.copyList(folderList, FolderVO.class));
    }

    /**
     * 读取文件（文本、文档、PDF等）
     *
     * @param response
     * @param userId
     * @param fileId
     */
    @GlobalInterceptor(admin = true)
    @RequestMapping("getFile/{userId}/{fileId}")
    public void getFile(HttpServletResponse response, @PathVariable("userId") String userId, @PathVariable("fileId") String fileId) {
        sysFileInfoService.getFile(response, fileId, userId);
    }

    /**
     * 读取视频文件（m3u8索引文件或视频分片文件）
     *
     * @param response
     * @param userId
     * @param fileId
     */
    @GlobalInterceptor(admin = true)
    @RequestMapping("getVideoFile/{userId}/{fileId}")
    public void getVideoFile(HttpServletResponse response, @PathVariable("userId") String userId, @PathVariable("fileId") String fileId) {
        sysFileInfoService.getFile(response, fileId, userId);
    }

    /**
     * 生成下载码
     *
     * @param userId
     * @param fileId
     * @return
     */
    @GlobalInterceptor(admin = true)
    @RequestMapping("createDownloadCode/{userId}/{fileId}")
    public BaseResponseVO createDownloadCode(@PathVariable("userId") String userId, @PathVariable("fileId") String fileId) {
        String downloadCode = sysFileInfoService.createDownloadCode(fileId, userId);
        return successResponse(downloadCode);
    }

    /**
     * 删除文件
     *
     * @param fileIdAndUserIds 格式：fileId1_userId1,fileId2_userId2
     * @return
     */
    @GlobalInterceptor(admin = true)
    @RequestMapping("deleteFile")
    public BaseResponseVO deleteFile(@Valid(required = true) String fileIdAndUserIds) {
        String[] fileIdAndUserIdArr = fileIdAndUserIds.split(",");
        for (String fileIdAndUserId : fileIdAndUserIdArr) {
            String[] arr = fileIdAndUserId.split("_");
            sysFileInfoService.deleteFile(arr[0], arr[1], true);
        }

        return successResponse(null);
    }
}
