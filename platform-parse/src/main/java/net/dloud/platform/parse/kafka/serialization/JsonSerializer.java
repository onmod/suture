package net.dloud.platform.parse.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.parse.utils.JacksonGet;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
public class JsonSerializer implements Serializer<KafkaMessage> {
    private ObjectMapper objectMapper = JacksonGet.SERIALIZER;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, KafkaMessage data) {
        if (data == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception ex) {
            throw new SerializationException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void close() {

    }
}
