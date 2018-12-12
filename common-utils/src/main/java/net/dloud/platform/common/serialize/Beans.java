package net.dloud.platform.common.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2017-06-07 16:09
 */
public class Beans {

    /**
     * 忽略对象中值为NULL或""的属性
     */
    private static Beans EXCLUDE_EMPTY;

    /**
     * 忽略对象中值为默认值的属性
     */
    private static Beans EXCLUDE_DEFAULT;

    /**
     * 转为蛇形
     */
    private static Beans SNAKE_CASE;

    /**
     * 默认不排除任何属性
     */
    private static Beans DEFAULT;

    private ObjectMapper mapper;


    private Beans() {
        mapper = new ObjectMapper();
        // ignore attributes exists in json string, but not in java object when deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private Beans(JsonInclude.Include include) {
        mapper = new ObjectMapper();
        // set serialization feature
        mapper.setSerializationInclusion(include);
        // ignore attributes exists in json string, but not in java object when deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private Beans(boolean flag) {
        mapper = new ObjectMapper();
        if (flag) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        } else {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        }
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static synchronized Beans getDefault() {
        if (null == DEFAULT) {
            DEFAULT = new Beans();
        }
        return DEFAULT;
    }

    public static synchronized Beans getSnakeCase() {
        if (null == SNAKE_CASE) {
            SNAKE_CASE = new Beans(true);
        }
        return SNAKE_CASE;
    }

    public static synchronized Beans getExcludeDefault() {
        if (null == EXCLUDE_DEFAULT) {
            EXCLUDE_DEFAULT = new Beans(JsonInclude.Include.NON_DEFAULT);
        }
        return EXCLUDE_DEFAULT;
    }

    public static synchronized Beans getExcludeEmpty() {
        if (null == EXCLUDE_EMPTY) {
            EXCLUDE_EMPTY = new Beans(JsonInclude.Include.NON_EMPTY);
        }
        return EXCLUDE_EMPTY;
    }

    public static Boolean isNullOrEmpty(Object o) {
        return o == null;
    }

    public <T> T beanCopy(Object source, Class<T> target) {
        if (isNullOrEmpty(source)) {
            return null;
        }
        return mapper.convertValue(source, target);
    }

    public Map<String, Object> bean2Map(Object target) {
        if (isNullOrEmpty(target)) {
            return null;
        }
        return mapper.convertValue(target, new TypeReference<Map<String, Object>>() {
        });
    }

    public Map<String, String> bean2StringMap(Object target) {
        if (isNullOrEmpty(target)) {
            return null;
        }
        return mapper.convertValue(target, new TypeReference<Map<String, String>>() {
        });
    }

    public <T> T map2Bean(Map<?, ?> map, Class<T> clazz) {
        if (isNullOrEmpty(map)) {
            return null;
        }
        return mapper.convertValue(map, clazz);
    }

    public Map<String, String> map2StringMap(Map<?, ?> map) {
        if (isNullOrEmpty(map)) {
            return null;
        }
        return mapper.convertValue(map, new TypeReference<Map<String, String>>() {
        });
    }

    public <T> List<T> list2Bean(List<?> list, Class<T> clazz) {
        if (isNullOrEmpty(list)) {
            return null;
        }
        return mapper.convertValue(list, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}
