package com.ywy.pojo.enums;

/**
 * 文件上传状态枚举值
 */
public enum UploadStatusEnum {
    UPLOAD_SECONDS("upload_seconds", "秒傳"),
    UPLOADING("uploading", "上傳中"),
    UPLOAD_FINISH("upload_finish", "上傳完成"),
    ;

    private String code;
    private String desc;

    UploadStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
