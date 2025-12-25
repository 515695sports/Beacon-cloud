package com.msb.api.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号格式校验工具
 */
public class PhoneFormatCheckUtil {
    //国内手机号的正则表达式
    //正则表达式编译器，只用编译一次，提高性能
    private static Pattern CHINA_PATTERN=Pattern.compile("^(?:\\+86)?1[3-9]\\d{9}$");

    //校验手机号是否合法
    public static boolean isChinaPhone(String number){
        Matcher matcher = CHINA_PATTERN.matcher(number);
        return matcher.matches();
    }
}
