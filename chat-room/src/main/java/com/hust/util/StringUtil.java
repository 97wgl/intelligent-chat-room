package com.hust.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * 移除字符串中的中文括号里面的内容
     */
    public static String removeBracket(String str) {
        return str.replaceAll("(\\()(.*)(\\))", "").replaceAll("（.*）", "");
    }

    /**
     * 去除字符串中的空格、回车、换行符、制表符等
     * @param str
     * @return
     */
    public static String removeSpecialChar(String str){
        String s = "";
        if (str != null) {
            // 定义含特殊字符的正则表达式
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            s = m.replaceAll("");
        }
        return s;
    }
}
