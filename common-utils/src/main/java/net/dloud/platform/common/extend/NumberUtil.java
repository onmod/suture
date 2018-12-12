package net.dloud.platform.common.extend;

import lombok.extern.slf4j.Slf4j;

/**
 * @author QuDasheng
 * @create 2016-09-22 11:03
 */
@Slf4j
public class NumberUtil {

    /**
     * 转换为long
     * @param obj
     * @return
     */
    public static int toInt(Object obj) {
        if (null == obj) {
            return 0;
        }

        return objToInt(obj.toString());
    }

    public static int toInt(String str) {
        if (null == str) {
            return 0;
        }

        return objToInt(str);
    }

    private static int objToInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            log.warn("转换为数字失败: ", e);
        }

        return 0;
    }

    /**
     * 转换为long
     * @param obj
     * @return
     */
    public static long toLong(Object obj) {
        if (null == obj) {
            return 0;
        }

        return objToLong(obj.toString());
    }

    public static long toLong(String str) {
        if (null == str) {
            return 0;
        }

        return objToLong(str);
    }

    private static long objToLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (Exception e) {
            log.warn("转换为数字失败: ", e);
        }

        return 0;
    }
}
