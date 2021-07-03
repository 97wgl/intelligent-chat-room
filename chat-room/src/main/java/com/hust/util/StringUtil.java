package com.hust.util;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * 移除字符串中的中文括号里面的内容
     */
    public static String removeBracket(String str) {
        return str.replaceAll("(\\()(.*)(\\))", "").replaceAll("（.*）", "");
    }
}
