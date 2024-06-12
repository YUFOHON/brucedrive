package com.ywy.pojo.enums;

/**
 * 文件分类枚举值
 */
public enum FileCategoryEnum {
    VIDEO(1, "video", "視訊"),
    AUDIO(2, "audio", "音訊"),
    IMAGE(3, "image", "圖片"),
    DOC(4, "doc", "文檔"),
    OTHERS(5, "others", "其他"),
    ;

    private Integer category;
    private String code;
    private String desc;

    FileCategoryEnum(Integer category, String code, String desc) {
        this.category = category;
        this.code = code;
        this.desc = desc;
    }

    public static FileCategoryEnum getByCode(String code) {
        for (FileCategoryEnum item : FileCategoryEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public Integer getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
