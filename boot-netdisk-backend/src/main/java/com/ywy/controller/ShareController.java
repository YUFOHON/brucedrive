package com.ywy.controller;

import com.ywy.core.annotation.GlobalInterceptor;
import com.ywy.core.annotation.Valid;
import com.ywy.core.base.BaseController;
import com.ywy.core.vo.BaseResponseVO;
import com.ywy.core.vo.PageResultVO;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.entity.SysFileShare;
import com.ywy.pojo.param.FileShareParam;
import com.ywy.pojo.vo.ShareInfoVO;
import com.ywy.service.SysFileShareService;
import com.ywy.util.BeanUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * file share controller
 */
@RestController
@RequestMapping("share")
public class ShareController extends BaseController {
    @Resource
    private SysFileShareService sysFileShareService;

    /**
     * 分页查询文件分享列表
     * @param session
     * @param param
     * @return
     */
    @GlobalInterceptor
    @RequestMapping("loadDataList")
    public BaseResponseVO loadDataList(HttpSession session, FileShareParam param) {
        param.setUserId(getUserInfoFromSession(session).getUserId());
        param.setQueryFileName(true);
        param.setOrderBy("create_time desc");
        PageResultVO pageResultVO = sysFileShareService.findListByPage(param);
        return successResponse(pageResultVO);
    }

    /**
     * 分享文件
     * @param session
     * @param fileId
     * @param validType
     * @param code
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("shareFile")
    public BaseResponseVO shareFile(HttpSession session, @Valid(required = true) String fileId,
                                    @Valid(required = true) Integer validType, String code) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        SysFileShare fileShare = sysFileShareService.shareFile(fileId, loginUserDto.getUserId(), validType, code);
        return successResponse(BeanUtil.copy(fileShare, ShareInfoVO.class));
    }

    /**
     * 取消分享
     * @param session
     * @param shareIds
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("cancelShare")
    public BaseResponseVO cancelShare(HttpSession session, @Valid(required = true) String shareIds) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        sysFileShareService.cancelShare(shareIds, loginUserDto.getUserId());
        return successResponse(null);
    }
}
