package com.ywy.core.exception;

import com.ywy.core.base.BaseController;
import com.ywy.core.enums.ResponseCodeEnum;
import com.ywy.core.vo.BaseResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理异常
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        log.error("请求错误，请求地址：{}，错误信息：", request.getRequestURL(), e);
        BaseResponseVO ajaxResponse = new BaseResponseVO();
        //404
        if (e instanceof NoHandlerFoundException) {
            ajaxResponse.setSuccess(false);
            ajaxResponse.setCode(ResponseCodeEnum.CODE_404.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_404.getMsg());
        } else if (e instanceof BusinessException) {
            //业务错误
            BusinessException biz = (BusinessException) e;
            ajaxResponse.setSuccess(false);
            ajaxResponse.setCode(biz.getCode() == null ? ResponseCodeEnum.CODE_601.getCode() : biz.getCode());
            ajaxResponse.setMsg(biz.getMessage());
        } else if (e instanceof BindException || e instanceof MethodArgumentTypeMismatchException) {
            //参数类型错误
            ajaxResponse.setSuccess(false);
            ajaxResponse.setCode(ResponseCodeEnum.CODE_602.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_601.getMsg());
        } else if (e instanceof DuplicateKeyException) {
            //主键冲突
            ajaxResponse.setSuccess(false);
            ajaxResponse.setCode(ResponseCodeEnum.CODE_603.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_603.getMsg());
        } else {
            ajaxResponse.setSuccess(false);
            ajaxResponse.setCode(ResponseCodeEnum.CODE_500.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_500.getMsg());
        }
        return ajaxResponse;
    }
}
