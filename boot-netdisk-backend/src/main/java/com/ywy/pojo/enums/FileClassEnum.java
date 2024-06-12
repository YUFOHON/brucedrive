package com.ywy.pojo.enums;

/**
 * 文件类型枚举值
 */
public enum FileClassEnum {
    FILE(1, "文件"),
    FOLDER(2, "目录"),
    ;

    private Integer type;
    private String desc;

    FileClassEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
