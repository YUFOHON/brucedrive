package com.ywy.core.enums;

/**
 * 接口返回状态枚举值
 */
public enum ResponseCodeEnum {
    // 通用HTTP请求
    CODE_200(200, "Request successful"),
    CODE_404(404, "Request address does not exist"),
    CODE_500(500, "Server abnormality"),
    // System related
    CODE_600(600, "Login timeout, please login again"),
    CODE_601(601, "Request abnormality, please contact the administrator"),
    CODE_602(602, "Request parameter error"),
    CODE_603(603, "Information already exists"),
    // Network disk related
    CODE_900(900, "Cannot move files to itself or its subdirectories"),
    CODE_901(901, "Insufficient network disk space, please expand"),
    CODE_902(902, "Shared link does not exist or has expired"),
    CODE_903(903, "Shared file has been deleted"),
    CODE_904(904, "Share verification is invalid, please re-verify");

    private Integer code;

    private String msg;

    ResponseCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
