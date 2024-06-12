package com.ywy.pojo.enums;

public enum ShareValidTypeEnum {
    DAY_1(0, 1, "1天"),
    DAY_7(1, 7, "1天"),
    DAY_30(2, 30, "1天"),
    FOREVER(3, -1, "永久"),
    ;

    private Integer type;
    private Integer days;
    private String desc;

    ShareValidTypeEnum(Integer type, Integer days, String desc) {
        this.type = type;
        this.days = days;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public Integer getDays() {
        return days;
    }

    public String getDesc() {
        return desc;
    }

    public static ShareValidTypeEnum getByType(Integer type) {
        for (ShareValidTypeEnum typeEnum : ShareValidTypeEnum.values()) {
            if (typeEnum.getType() == type) {
                return typeEnum;
            }
        }
        return null;
    }
}
