package com.komodo.community.utils;

import com.sun.istack.internal.Nullable;

/**
 * @Author ZhangGJ
 * @Date 2021/04/12 22:44
 */
public class StringUtils {

    public static boolean hasText(@Nullable String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    public static boolean containsText(CharSequence str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
