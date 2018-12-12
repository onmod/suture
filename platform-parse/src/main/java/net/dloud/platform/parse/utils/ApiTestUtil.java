package net.dloud.platform.parse.utils;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.serialize.Beans;
import net.dloud.platform.extend.exception.PassedException;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * 从容器中直接获取bean
 *
 * @author: Qudasheng
 * @create: 2018-05-12 12:37
 **/
@Slf4j
public class ApiTestUtil {
    private static final Beans beans = Beans.getDefault();

    public static Class<?> getInterface(String name, Class<?>[] inter) {
        if (inter.length < 1) {
            return null;
        } else if (inter.length == 1) {
            return inter[0];
        } else {
            for (Class<?> one : inter) {
                if (name.startsWith(one.getSimpleName())) {
                    return one;
                }
            }
            return inter[0];
        }
    }

    public static String toLowerCaseFirst(String input) {
        if (null == input || input.isEmpty()) {
            return "";
        } else {
            final String first = String.valueOf(input.charAt(0));
            return input.replaceFirst(first, first.toLowerCase());
        }
    }

    public static Object[] sortInputByMethod(Map<String, Object> map, List<String> names,
                                             Parameter[] parameters) {
        Object[] params = new Object[names.size()];
        int i = 0;
        for (String name : names) {
            final Parameter parameter = parameters[i];
            final Object get = map.get(name);
            if (null == get) {
                throw new PassedException("参数: [" + name + "]为空");
            }
            params[i] = convertToExactType(get, parameter.getType());
            i += 1;
        }
        return params;
    }

    public static Object convertToExactType(Object value, Class<?> newType) {
        final String simpleName = newType.getSimpleName();
        if ("Long".equals(simpleName)) {
            return Long.valueOf(value.toString());
        } else if ("Integer".equals(simpleName)) {
            return Integer.valueOf(value.toString());
        } else if ("Short".equals(simpleName)) {
            return Short.valueOf(value.toString());
        } else if ("Byte".equals(simpleName)) {
            return Byte.valueOf(value.toString());
        } else if ("Character".equals(simpleName)) {
            return value.toString();
        } else if ("Boolean".equals(simpleName)) {
            return Boolean.valueOf(value.toString());
        } else if ("Float".equals(simpleName)) {
            return Float.valueOf(value.toString());
        } else if ("Double".equals(simpleName)) {
            return Double.valueOf(value.toString());
        } else if ("String".equals(simpleName)) {
            return value;
        } else {
            return beans.beanCopy(value, newType);
        }
    }
}