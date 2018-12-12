package net.dloud.platform.parse.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author QuDasheng
 * @create 2018-11-29 16:33
 **/
public class JacksonGet {
    public static final ObjectMapper SERIALIZER;

    static {
        SERIALIZER = new ObjectMapper();
        SERIALIZER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        SERIALIZER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        SERIALIZER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        SERIALIZER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
