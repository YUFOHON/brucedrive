package com.ywy.core.vo;

import lombok.Data;

/**
 * 接口返回信息
 * @param <T>
 */
@Data
public class BaseResponseVO<T> {
    /**
     * 状态信息：0成功；1失败
     */
    private Boolean success;

    /**
     * 状态码：200成功，其他失败
     */
    private Integer code;

    /**
     * 描述信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;
}
