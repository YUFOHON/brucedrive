package com.ywy.util;

import com.ywy.core.enums.VerifyRegexEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidUtil {
    public static boolean verify(String regex, String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static boolean verify(VerifyRegexEnum regex, String value) {
        return verify(regex.getRegex(), value);
    }
}
