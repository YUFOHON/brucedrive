package com.ywy.core.enums;

import lombok.Getter;

/**
 * 参数校验正则表达式枚举值
 */
@Getter
public enum VerifyRegexEnum {
    NO("", "不校验"),
    IP("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}", "IP地址"),
    POSITIVE_INTEGER("^[0-9]*[1-9][0-9]*$", "正整数"),
    NUMBER_LETTER_UNDER_LINE("^\\w+$", "由數字、26個英文字母或底線組成的字串"),
    EMAIL("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$", "郵箱"),
    PHONE("(1[0-9])\\d{9}$", "手機號碼"),
    COMMON("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", "數字、字母、中文、底線"),
    PASSWORD("^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,}$", "必須包含數字、字母和特殊字符，長度至少8位"),
    ACCOUNT("^[0-9a-zA-Z_]{1,}$", "字母開頭，由數字、英文字母或底線組成"),
    MONEY("^[0-9]+(.[0-9]{1,2})?$", "金額")
    ;
    private String regex;
    private String desc;
    VerifyRegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }
}
