package net.dloud.platform.common.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dloud.platform.common.exception.SerializeException;
import net.dloud.platform.common.extend.StringUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2017-06-07 16:09
 */
public class Jsons {
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 忽略对象中值为NULL或""的属性
     */
    private static Jsons EXCLUDE_EMPTY;

    /**
     * 忽略对象中值为默认值的属性
     */
    private static Jsons EXCLUDE_DEFAULT;

    /**
     * 默认不排除任何属性
     */
    private static Jsons DEFAULT;

    private ObjectMapper mapper;


    private Jsons() {
        mapper = new ObjectMapper();
        // ignore attributes exists in json string, but not in java object when deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
    }

    private Jsons(JsonInclude.Include include) {
        mapper = new ObjectMapper();
        // set serialization feature
        mapper.setSerializationInclusion(include);
        // ignore attributes exists in json string, but not in java object when deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
    }

    public static synchronized Jsons getDefault() {
        if (null == DEFAULT) {
            DEFAULT = new Jsons();
        }
        return DEFAULT;
    }

    public static synchronized Jsons getExcludeDefault() {
        if (null == EXCLUDE_DEFAULT) {
            EXCLUDE_DEFAULT = new Jsons(JsonInclude.Include.NON_DEFAULT);
        }
        return EXCLUDE_DEFAULT;
    }

    public static synchronized Jsons getExcludeEmpty() {
        if (null == EXCLUDE_EMPTY) {
            EXCLUDE_EMPTY = new Jsons(JsonInclude.Include.NON_EMPTY);
        }
        return EXCLUDE_EMPTY;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String toJson(Object target) {
        if (null == target) {
            return null;
        }
        try {
            return mapper.writeValueAsString(target);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public Map<String, Object> mapJson(String json) {
        if (StringUtil.isBlank(json)) {
            return Collections.emptyMap();
        }
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public List<Object> listJson(String json) {
        if (StringUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<Object>>() {
            });
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public Map<String, String> mapStringJson(String json) {
        if (StringUtil.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtil.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, JavaType javaType) {
        if (StringUtil.isBlank(json)) {
            return null;
        }
        try {
            return (T) mapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new SerializeException(e);
        }
    }

    public <T> T fromJson(String json, TypeReference type) {
        if (StringUtil.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}