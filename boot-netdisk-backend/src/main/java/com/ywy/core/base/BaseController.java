package com.ywy.core.base;

import com.ywy.core.constants.SystemConstants;
import com.ywy.core.enums.ResponseCodeEnum;
import com.ywy.core.exception.BusinessException;
import com.ywy.core.vo.BaseResponseVO;
import com.ywy.core.vo.PageResultVO;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.dto.SessionShareDto;
import com.ywy.util.BeanUtil;

import javax.servlet.http.HttpSession;

/**
 * Basic Controller
 */
public class BaseController {
    /**
     * return success information
     * @param t
     * @param <T>
     * @return
     */
    protected <T> BaseResponseVO successResponse(T t) {
        BaseResponseVO<T> responseVO = new BaseResponseVO<>();
        responseVO.setSuccess(true);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setMsg(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    /**
     * return system error information
     * @param t
     * @param <T>
     * @return
     */
    protected <T> BaseResponseVO errorRespnseServer(T t) {
        BaseResponseVO vo = new BaseResponseVO();
        vo.setSuccess(false);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setMsg(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    /**
     * return custom error information
     * @param e
     * @param t
     * @param <T>
     * @return
     */
    protected <T> BaseResponseVO errorResponseBusiness(BusinessException e, T t) {
        BaseResponseVO vo = new BaseResponseVO();
        vo.setSuccess(false);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_601.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setMsg(e.getMessage());
        vo.setData(t);
        return vo;
    }

    /**
     * get user information from session
     * @param session
     * @return
     */
    protected SessionLoginUserDto getUserInfoFromSession(HttpSession session) {
        return (SessionLoginUserDto) session.getAttribute(SystemConstants.SESSION_LOGIN_USER);
    }

    /**
     * get file information from session
     * @param session
     * @param shareId
     * @return
     */
    protected SessionShareDto getShareInfoFromSession(HttpSession session, String shareId) {
        return (SessionShareDto) session.getAttribute(SystemConstants.SESSION_SHARE + shareId);
    }

    /**
     * Convert pagination objects
     *
     * The purpose of this method is to create a new PageResultVO<T> instance and populate it
     * with the data from the input PageResultVO<S> instance, but with the list of objects
     * converted from type S to type T.
     * @param vo
     * @param clazz
     * @param <S>
     * @param <T>
     * @return
     */
    protected <S, T> PageResultVO<T> covertPageResultVO(PageResultVO<S> vo, Class<T> clazz) {
        PageResultVO<T> resultVO = new PageResultVO<>();
        resultVO.setList(BeanUtil.copyList(vo.getList(), clazz));
        resultVO.setPageNo(vo.getPageNo());
        resultVO.setPageSize(vo.getPageSize());
        resultVO.setPageTotal(vo.getPageTotal());
        resultVO.setTotalCount(vo.getTotalCount());
        return resultVO;
    }

}
