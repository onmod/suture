package net.dloud.platform.common.extend;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;

/**
 * @author QuDasheng
 * @create 2016-09-22 11:03
 */
public class StringUtil {

    /**
     * 判断字符串是否为null或""
     *
     * @param str 字符串
     * @return 若为null或""返回true，反之false
     */
    public static Boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static Boolean notBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 数组转字符串
     *
     * @param array
     * @param separator
     * @return
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }

        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length())
                + separator.length());

        StringBuilder buf = new StringBuilder(bufSize);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * 首字母大写
     *
     * @param str 字符串
     * @return
     */
    public static String firstUpperCase(String str) {
        final String strFirst = String.valueOf(str.charAt(0));
        return str.replaceFirst(strFirst, strFirst.toUpperCase());
    }

    /**
     * 首字母小写
     *
     * @param str 字符串
     * @return
     */
    public static String firstLowerCase(String str) {
        final String strFirst = String.valueOf(str.charAt(0));
        return str.replaceFirst(strFirst, strFirst.toLowerCase());
    }

    /**
     * 驼峰转下划线小写
     *
     * @param str 字符串
     * @return
     */
    public static String camel2UnderLower(String str) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str);
    }

    /**
     * 驼峰转下划线大写
     *
     * @param str 字符串
     * @return
     */
    public static String camel2UnderUpper(String str) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, str);
    }

    /**
     * 返回类名的后缀
     *
     * @param str
     * @return
     */
    public static String classSuffix(String str) {
        if (isBlank(str)) {
            return "";
        }
        int lastIndexIn = CharMatcher.inRange('A', 'Z').lastIndexIn(str);
        if (lastIndexIn < 0) {
            return "";
        } else {
            return str.substring(lastIndexIn);
        }
    }

    /**
     * 按逗号分割并返回第一个
     *
     * @param str
     * @return
     */
    public static String splitFirstByComa(String str) {
        return splitBy(str, "\\,", 0);
    }

    /**
     * 按逗号分割并返回最后一个
     *
     * @param str
     * @return
     */
    public static String splitLastByComa(String str) {
        return splitBy(str, "\\,", -1);
    }

    /**
     * 按点分割并返回最后一个
     *
     * @param str
     * @return
     */
    public static String splitLastByDot(String str) {
        return splitBy(str, "\\.", -1);
    }

    private static String splitBy(String str, String by, int idx) {
        if (isBlank(str)) {
            return "";
        }

        String res = "";
        String[] split = str.split(by);
        final int length = split.length;
        if (length > 0) {
            if (idx == -1 || idx > length - 1) {
                res = split[length - 1];
            } else if (idx < -1) {
                res = split[0];
            } else {
                res = split[idx];
            }
        }

        if (null == res) res = "";

        return res;
    }
}
