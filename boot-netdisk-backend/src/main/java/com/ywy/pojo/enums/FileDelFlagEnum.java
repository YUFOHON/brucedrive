package com.ywy.pojo.enums;

/**
 * 文件删除标记枚举值
 */
public enum FileDelFlagEnum {
    USING(0, "正常"),
    RECYCLE(1, "回收站"),
    DEL(2, "删除"),
    ;

    private Integer flag;
    private String desc;

    FileDelFlagEnum(Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public Integer getFlag() {
        return flag;
    }

    public String getDesc() {
        return desc;
    }
}
