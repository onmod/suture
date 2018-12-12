package net.dloud.platform.maven;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-15 23:50
 **/
public class InjectionUtil {
    public static final String DEFAULT_VALUE = "__DEFAULT__";

    private static Map<String, String> injectionEnum = new HashMap<>();


    /**
     * 枚举值和其方法的map
     *
     * @return
     */
    public static Map<String, String> getInjectionEnum(String injectEnum) {
        if (injectionEnum.isEmpty()) {
            return resolveInjectionEnum(injectEnum);
        } else {
            return injectionEnum;
        }
    }

    /**
     * 注入的枚举值，从中获取方法
     *
     * @return
     */
    private static Map<String, String> resolveInjectionEnum(String injectEnum) {
        try {
            final Class<?> clazz = Class.forName(injectEnum);
            final Field[] fields = clazz.getFields();
            final Method method = clazz.getMethod("method");
            final Method acquire = clazz.getMethod("acquire");

            final Map<String, String> map = Maps.newHashMapWithExpectedSize(fields.length);
            for (Field field : fields) {
                final Object obj = field.get(clazz);
                if (null == map.get(DEFAULT_VALUE)) {
                    map.put(DEFAULT_VALUE, String.valueOf(acquire.invoke(obj)));
                }
                map.put(field.getName(), String.valueOf(method.invoke(obj)));
            }
            map.put("", map.get(DEFAULT_VALUE));
            injectionEnum = map;
            return map;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
