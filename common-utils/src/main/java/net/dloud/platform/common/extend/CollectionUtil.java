package net.dloud.platform.common.extend;

import net.dloud.platform.common.serialize.InnerTypeUtil;

import java.util.Collection;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-18 23:22
 **/
public class CollectionUtil {
    public static boolean isEmpty(Map<?, ?> input) {
        if (null == input || input.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean notEmpty(Map<?, ?> input) {
        return !isEmpty(input);
    }

    public static boolean isEmpty(Collection<?> input) {
        if (null == input || input.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean notEmpty(Collection<?> input) {
        return !isEmpty(input);
    }

    public static boolean isList(String name) {
        return "List".equalsIgnoreCase(StringUtil.classSuffix(name)) && InnerTypeUtil.isJavaType(name);
    }

    public static boolean isMap(String name) {
        return "Map".equalsIgnoreCase(StringUtil.classSuffix(name)) && InnerTypeUtil.isJavaType(name);
    }
}
