package com.ywy.controller;

import com.ywy.core.annotation.GlobalInterceptor;
import com.ywy.core.annotation.Valid;
import com.ywy.core.base.BaseController;
import com.ywy.core.vo.BaseResponseVO;
import com.ywy.core.vo.PageResultVO;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.enums.FileDelFlagEnum;
import com.ywy.pojo.param.FileInfoParam;
import com.ywy.pojo.vo.FileInfoVO;
import com.ywy.service.SysFileInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * Recycle Bin related
 */
@RestController
@RequestMapping("recycle")
public class RecycleController extends BaseController {
    @Resource
    private SysFileInfoService sysFileInfoService;

    /**
     * Paging query Recycle Bin file list
     * @param session
     * @param param
     * @return
     */
    @GlobalInterceptor
    @RequestMapping("loadDataList")
    public BaseResponseVO loadDataList(HttpSession session, FileInfoParam param) {
        param.setUserId(getUserInfoFromSession(session).getUserId());
        param.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        param.setOrderBy("recycle_time desc");
        PageResultVO pageResultVO = sysFileInfoService.findListByPage(param);
        return successResponse(covertPageResultVO(pageResultVO, FileInfoVO.class));
    }

    /**
     * Restore files
     * @param session
     * @param fileIds
     * @return
     */
    @GlobalInterceptor
    @RequestMapping("revertFile")
    public BaseResponseVO revertFile(HttpSession session, @Valid(required = true) String fileIds) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        sysFileInfoService.revertFile(fileIds, loginUserDto.getUserId());
        return successResponse(null);
    }

    /**
     * Completely delete files
     * @param session
     * @param fileIds
     * @return
     */
    @GlobalInterceptor
    @RequestMapping("deleteFile")
    public BaseResponseVO deleteFile(HttpSession session, @Valid(required = true) String fileIds) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        sysFileInfoService.deleteFile(fileIds, loginUserDto.getUserId(), false);
        return successResponse(null);
    }
}