package com.ywy.pojo.enums;

/**
 * 文件状态枚举值
 */
public enum FileStatusEnum {
    TRANSFER(0, "转码中"),
    TRANSFER_FILE(1, "转码失败"),
    USING(2, "使用中"),
    RECYCLE(3, "回收站"),
    DEL(4, "已删除"),
    ;

    private Integer status;
    private String desc;

    FileStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
