package net.dloud.platform.parse.kafka.serialization;

import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
public class KryoSerializer implements Serializer<KafkaMessage> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, KafkaMessage data) {
        return KryoBaseUtil.writeObjectToByteArray(data);
    }

    @Override
    public void close() {

    }
}
