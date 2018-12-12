package net.dloud.platform.parse.initial;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.kafka.listener.KafkaMessageListener;
import net.dloud.platform.parse.kafka.serialization.KryoDeserializer;
import net.dloud.platform.parse.kafka.serialization.KryoSerializer;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-03 15:17
 **/
@Slf4j
@EnableKafka
@Configuration
@ConditionalOnProperty(name = "kafka.init.enable", matchIfMissing = true, havingValue = "true")
@AutoConfigureAfter(InitDefault.class)
public class InitKafka {
    @Value("${kafka.broker.list}")
    private String brokerList;

    @Value("${kafka.listener.concurrency:2}")
    private Integer listenerConcurrency;

    @Autowired
    private KafkaMessageListener messageListener;

    /**
     * 生产者工厂
     */
    public ProducerFactory<Long, KafkaMessage> producerFactory() {
        Map<String, Object> props = Maps.newHashMapWithExpectedSize(8);
        // kafka.metadata.broker.list=10.16.0.214:9092,10.16.0.215:9092,10.16.0.216:9092
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        props.put(ProducerConfig.RETRIES_CONFIG, 4);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 4096);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 40960);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KryoSerializer.class);
        final DefaultKafkaProducerFactory<Long, KafkaMessage> producerFactory = new DefaultKafkaProducerFactory<>(props);
        producerFactory.setTransactionIdPrefix(PlatformConstants.KAFKA_TOPIC);
        return producerFactory;
    }

    /**
     * 消费者者工厂
     */
    public ConsumerFactory<Long, KafkaMessage> consumerFactory() {
        Map<String, Object> props = Maps.newHashMapWithExpectedSize(10);
        // kafka.metadata.broker.list=10.16.0.214:9092,10.16.0.215:9092,10.16.0.216:9092
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KryoDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, PlatformConstants.KAFKA_CONSUMER_GROUP);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 生产者模版
     */
    @Bean
    @Primary
    public KafkaTemplate<Long, KafkaMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 消费者配置
     */
    @Bean
    public ConcurrentMessageListenerContainer<Long, KafkaMessage> messageListenerContainer() {
        final ContainerProperties containerProperties = new ContainerProperties(PlatformConstants.KAFKA_TOPIC);
        containerProperties.setMessageListener(messageListener);
        containerProperties.setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL);

        final ConcurrentMessageListenerContainer<Long, KafkaMessage> listenerContainer =
                new ConcurrentMessageListenerContainer<>(consumerFactory(), containerProperties);
        listenerContainer.setConcurrency(listenerConcurrency);
        listenerContainer.setAutoStartup(true);
        return listenerContainer;
    }
}
