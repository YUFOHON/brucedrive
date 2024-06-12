package com.ywy.controller;

import com.ywy.core.annotation.GlobalInterceptor;
import com.ywy.core.annotation.Valid;
import com.ywy.core.base.BaseController;
import com.ywy.core.constants.SystemConstants;
import com.ywy.core.enums.ResponseCodeEnum;
import com.ywy.core.exception.BusinessException;
import com.ywy.core.vo.BaseResponseVO;
import com.ywy.core.vo.PageResultVO;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.dto.SessionShareDto;
import com.ywy.pojo.entity.SysFileInfo;
import com.ywy.pojo.entity.SysFileShare;
import com.ywy.pojo.entity.SysUser;
import com.ywy.pojo.enums.FileDelFlagEnum;
import com.ywy.pojo.param.FileInfoParam;
import com.ywy.pojo.vo.FileInfoVO;
import com.ywy.pojo.vo.FolderVO;
import com.ywy.pojo.vo.WebShareVO;
import com.ywy.service.SysFileInfoService;
import com.ywy.service.SysFileShareService;
import com.ywy.service.SysUserService;
import com.ywy.util.BeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * External sharing, no login required
 */
@RestController
@RequestMapping("webShare")
public class WebShareController extends BaseController {
    @Resource
    private SysFileShareService sysFileShareService;
    @Resource
    private SysFileInfoService sysFileInfoService;
    @Resource
    private SysUserService sysUserService;

    /**
     * Get sharing information
     * If the extraction code is not verified, the front end jumps to the page number for entering the extraction code
     *
     * @param session
     * @param shareId
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("getShareInfo")
    public BaseResponseVO getShareInfo(HttpSession session, @Valid(required = true) String shareId) {
// The sharing link does not exist or has expired
        SysFileShare fileShare = sysFileShareService.selectByShareId(shareId);
        if (fileShare == null || new Date().after(fileShare.getExpireTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }

// The shared file has been deleted
        SysFileInfo fileInfo = sysFileInfoService.selectByFileIdAndUserId(fileShare.getFileId(), fileShare.getUserId());
        if (fileInfo == null || FileDelFlagEnum.USING.getFlag() != fileInfo.getDelFlag()) {
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }

// Get file sharing information from session
        SessionShareDto shareDto = getShareInfoFromSession(session, shareId);
// If it does not exist, it means that the extraction code has not been verified. Return null and the front end will jump to the extraction code input page
        if (shareDto  == null) {
            return successResponse(null);
        }

// Get sharing user information
        SysUser user = sysUserService.selectByUserId(fileShare.getUserId());

// Assemble sharing information
        WebShareVO webShareVO = BeanUtil.copy(fileShare, WebShareVO.class);
        webShareVO.setFileName(fileInfo.getFileName());
        webShareVO.setNickName(user.getNickName());
        webShareVO.setAvatar(user.getQqAvatar());

// Determine whether it is a file shared by the current user
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        if (loginUserDto != null && loginUserDto.getUserId().equals(shareDto.getShareUserId())) {
            webShareVO.setCurrentUser(true);
        } else {
            webShareVO.setCurrentUser(false);
        }

        return successResponse(webShareVO);
    }

    /**
     * Get sharing information
     *
     * @param shareId
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("getShareFile")
    public BaseResponseVO getShareFile(@Valid(required = true) String shareId) {
// The sharing link is invalid or expired
        SysFileShare fileShare = sysFileShareService.selectByShareId(shareId);
        if (fileShare == null || new Date().after(fileShare.getExpireTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }

        // The shared file has been deleted
        SysFileInfo fileInfo = sysFileInfoService.selectByFileIdAndUserId(fileShare.getFileId(), fileShare.getUserId());
        if (fileInfo == null || FileDelFlagEnum.USING.getFlag() != fileInfo.getDelFlag()) {
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }

        // Get sharing user information
        SysUser user = sysUserService.selectByUserId(fileShare.getUserId());

        // Assemble sharing information
        WebShareVO webShareVO = BeanUtil.copy(fileShare, WebShareVO.class);
        webShareVO.setFileName(fileInfo.getFileName());
        webShareVO.setNickName(user.getNickName());
        webShareVO.setAvatar(user.getQqAvatar());

        return successResponse(webShareVO);
    }

    /**
     * Verify extraction code
     *
     * @param session
     * @param shareId
     * @param code
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("checkShareCode")
    public BaseResponseVO checkShareCode(HttpSession session, @Valid(required = true) String shareId,
                                         @Valid(required = true) String code) {
        SessionShareDto shareDto = sysFileShareService.checkShareCode(shareId, code);
        session.setAttribute(SystemConstants.SESSION_SHARE + shareId, shareDto);
        return successResponse(null);
    }

    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("loadFileList")
    public BaseResponseVO loadFileList(HttpSession session, @Valid(required = true) String shareId, String filePid) {
        SessionShareDto shareDto = checkShare(session, shareId);

        FileInfoParam param = new FileInfoParam();
        // Check the specified directory for shared files
        //the user is trying to access a file within a specific directory
        if (StringUtils.isNotEmpty(filePid) && !SystemConstants.ZERO_STR.equals(filePid)) {
        // Check if the directory belongs to shared files
            sysFileInfoService.checkRootFilePid(shareDto.getFileId(), filePid, shareDto.getShareUserId());

            param.setFilePid(filePid);
        } else {
            // 查看分享文件
            param.setFileId(shareDto.getFileId());
        }
        param.setUserId(shareDto.getShareUserId());
        param.setDelFlag(FileDelFlagEnum.USING.getFlag());
        param.setOrderBy("update_time desc");
        PageResultVO<SysFileInfo> pageResultVO = sysFileInfoService.findListByPage(param);
        return successResponse(covertPageResultVO(pageResultVO, FileInfoVO.class));
    }
    /**
     * Get directory list based on path
     *
     * @param session
     * @param shareId
     * @param path
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("getFolderList")
    public BaseResponseVO getFolderList(HttpSession session, @Valid(required = true) String shareId,
                                        @Valid(required = true) String path) {
        SessionShareDto shareDto = checkShare(session, shareId);
        List<SysFileInfo> folderList = sysFileInfoService.getFolderList(path, shareDto.getShareUserId());
        return successResponse(BeanUtil.copyList(folderList, FolderVO.class));
    }

    /**
     * Read files (text, documents, PDF, etc.)
     *
     * @param response
     * @param shareId
     * @param fileId
     */
    @GlobalInterceptor(login = false)
    @RequestMapping("getFile/{shareId}/{fileId}")
    public void getFile(HttpSession session, HttpServletResponse response, @PathVariable("shareId") String shareId,
                        @PathVariable("fileId") String fileId) {
        SessionShareDto shareDto = checkShare(session, shareId);
        sysFileInfoService.getFile(response, fileId, shareDto.getShareUserId());
    }

    /**
     * Read video file (m3u8 index file or video segment file)
     *
     * @param response
     * @param shareId
     * @param fileId
     */
    @GlobalInterceptor(login = false)
    @RequestMapping("getVideoFile/{shareId}/{fileId}")
    public void getVideoFile(HttpSession session, HttpServletResponse response, @PathVariable("shareId") String shareId,
                             @PathVariable("fileId") String fileId) {
        SessionShareDto shareDto = checkShare(session, shareId);
        sysFileInfoService.getFile(response, fileId, shareDto.getShareUserId());
    }

    /**
     * Generate download code
     *
     * @param shareId
     * @param fileId
     * @return
     */
    @GlobalInterceptor(login = false)
    @RequestMapping("createDownloadCode/{shareId}/{fileId}")
    public BaseResponseVO createDownloadCode(HttpSession session, @PathVariable("shareId") String shareId,
                                             @PathVariable("fileId") String fileId) {
        SessionShareDto shareDto = checkShare(session, shareId);
        String downloadCode = sysFileInfoService.createDownloadCode(fileId, shareDto.getShareUserId());
        return successResponse(downloadCode);
    }

    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("saveShare")
    public BaseResponseVO saveShare(HttpSession session, @Valid(required = true) String shareId,
                                    @Valid(required = true) String shareFileIds, @Valid(required = true) String targetFolderId) {
        SessionShareDto shareDto = checkShare(session, shareId);
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        if (shareDto.getShareUserId().equals(loginUserDto.getUserId())) {
            throw new BusinessException("The files you shared cannot be saved to your own network disk");
        }

        sysFileInfoService.saveShare(shareDto.getFileId(), shareFileIds, targetFolderId, shareDto.getShareUserId(), loginUserDto.getUserId());

        return successResponse(null);
    }

    /**
     * Verify sharing
     *
     * @param session
     * @param shareId
     * @return
     */
    private SessionShareDto checkShare(HttpSession session, String shareId) {
// Sharing verification is invalid, please re-verify
        SessionShareDto shareDto = getShareInfoFromSession(session, shareId);
        if (shareDto  == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }

// Sharing link has expired
        if (new Date().after(shareDto.getExpireTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }

        return shareDto;
    }
}
