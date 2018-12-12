package net.dloud.platform.parse.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.parse.utils.JacksonGet;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
public class JsonDeserializer implements Deserializer<KafkaMessage> {
    private ObjectMapper objectMapper = JacksonGet.SERIALIZER;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public KafkaMessage deserialize(String topic, byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        try {
            return this.objectMapper.readValue(bytes, 0, bytes.length, KafkaMessage.class);
        } catch (Exception ex) {
            throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void close() {

    }
}
