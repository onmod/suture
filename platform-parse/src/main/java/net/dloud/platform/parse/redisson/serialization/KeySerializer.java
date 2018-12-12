package net.dloud.platform.parse.redisson.serialization;

import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author QuDasheng
 * @create 2018-10-22 15:17
 **/
public class KeySerializer implements RedisSerializer<String> {
    private final String prefix = PlatformConstants.APPID + "-" + PlatformConstants.MODE + "-";

    private final Charset charset;


    public KeySerializer() {
        this(StandardCharsets.UTF_8);
    }

    public KeySerializer(Charset charset) {
        AssertWrapper.notNull(charset, "字符集设置不能为空");
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] data) {
        if (null == data) {
            return null;
        }

        String str = new String(data, charset);
        return str.replaceFirst(prefix, "");
    }

    @Override
    public byte[] serialize(String data) {
        return (data == null ? null : (prefix + data).getBytes(charset));
    }
}
