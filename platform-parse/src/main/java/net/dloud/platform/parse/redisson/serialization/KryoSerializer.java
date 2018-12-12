package net.dloud.platform.parse.redisson.serialization;

import net.dloud.platform.common.serialize.KryoBaseUtil;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
public class KryoSerializer implements RedisSerializer<Object> {

    @Override
    public Object deserialize(@Nullable byte[] data) throws SerializationException {
        if (null == data || data.length == 0) {
            return null;
        }

        try {
            return KryoBaseUtil.readFromByteArray(data);
        } catch (Exception ex) {
            throw new SerializationException("序列化失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public byte[] serialize(@Nullable Object data) throws SerializationException {
        if (null == data) {
            return new byte[0];
        }

        try {
            return KryoBaseUtil.writeToByteArray(data);
        } catch (Exception ex) {
            throw new SerializationException("序列化失败: " + ex.getMessage(), ex);
        }
    }
}
