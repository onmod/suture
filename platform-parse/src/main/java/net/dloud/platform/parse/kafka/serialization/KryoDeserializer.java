package net.dloud.platform.parse.kafka.serialization;

import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
public class KryoDeserializer implements Deserializer<KafkaMessage> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public KafkaMessage deserialize(String topic, byte[] data) {
        return KryoBaseUtil.readObjectFromByteArray(data, KafkaMessage.class);
    }

    @Override
    public void close() {

    }
}
