package net.dloud.platform.common.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.dloud.platform.common.exception.SerializeException;
import net.dloud.platform.common.extend.StreamUtil;
import net.dloud.platform.common.extend.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2017-06-07 16:09
 **/
public class Xmls {
    /**
     * 忽略对象中值为NULL或""的属性
     */
    private static Xmls EXCLUDE_EMPTY;

    /**
     * 忽略对象中值为默认值的属性
     */
    private static Xmls EXCLUDE_DEFAULT;

    /**
     * 默认不排除任何属性
     */
    private static Xmls DEFAULT;

    private ObjectMapper mapper;


    private Xmls() {
        mapper = new XmlMapper();
        // ignore attributes exists in xml string, but not in java object when deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private Xmls(JsonInclude.Include include) {
        mapper = new XmlMapper();
        // set serialization feature
        mapper.setSerializationInclusion(include);
        // ignore attributes exists in xml string, but not in java object when deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static synchronized Xmls getDefault() {
        if (null == DEFAULT) {
            DEFAULT = new Xmls();
        }
        return DEFAULT;
    }

    public static synchronized Xmls getExcludeDefault() {
        if (null == EXCLUDE_DEFAULT) {
            EXCLUDE_DEFAULT = new Xmls(JsonInclude.Include.NON_DEFAULT);
        }
        return EXCLUDE_DEFAULT;
    }

    public static synchronized Xmls getExcludeEmpty() {
        if (null == EXCLUDE_EMPTY) {
            EXCLUDE_EMPTY = new Xmls(JsonInclude.Include.NON_EMPTY);
        }
        return EXCLUDE_EMPTY;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String toXml(Object target) {
        try {
            return mapper.writeValueAsString(target);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public Map<String, Object> fromXml(String xml) {
        if (StringUtil.isBlank(xml)) {
            return null;
        }
        try {
            return mapper.readValue(xml, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    public <T> T fromXml(String xml, Class<T> clazz) {
        if (StringUtil.isBlank(xml)) {
            return null;
        }
        try {
            return mapper.readValue(xml, clazz);
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T fromXml(String xmlString, JavaType javaType) {
        if (StringUtil.isBlank(xmlString)) {
            return null;
        }
        try {
            return (T) mapper.readValue(xmlString, javaType);
        } catch (Exception e) {
            throw new SerializeException(e);
        }
    }

    public JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}
