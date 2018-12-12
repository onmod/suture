package net.dloud.platform.parse.module;

import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.extend.constant.CenterEnum;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import net.dloud.platform.parse.kafka.SimpleMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.dloud.platform.parse.utils.SourceGet.kafkaProof;

/**
 * @author QuDasheng
 * @create 2018-10-07 12:37
 **/
@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.init.enable", matchIfMissing = true, havingValue = "true")
public class MessageComponent {
    @Autowired
    private KafkaTemplate<Long, KafkaMessage> kafkaTemplate;

    /**
     * 发送消息到本系统
     */
    public boolean sendMessage(final SimpleMessage receive) {
        return sendMessage(receive, Collections.singletonList(PlatformConstants.KAFKA_TOPIC));
    }

    /**
     * 发送消息
     */
    public boolean sendMessage(final CenterEnum center, final SimpleMessage receive) {
        return sendMessage(receive, Collections.singletonList(center.getTopic()));
    }

    public boolean sendMessage(final List<CenterEnum> centers, final SimpleMessage receive) {
        return sendMessage(receive, centers.stream().map(CenterEnum::getTopic).collect(Collectors.toList()));
    }

    private boolean sendMessage(final SimpleMessage receive, final List<String> topics) {
        AssertWrapper.notNull(receive, "要发送的消息不能为空");
        final String bean = receive.getBean();
        AssertWrapper.notEmpty(bean, "消费者配置不能为空");
        final long key = receive.getKey();
        final Object input = receive.getInput();
        AssertWrapper.notNull(input, "要发送的消息不能为空");
        AssertWrapper.notEmpty(topics, "要发送到的topic不能为空");

        //如何存在说明是外部调用以这个为准
        final String proof = RpcContext.getContext().getAttachment(PlatformConstants.PROOF_KEY);
        final KafkaMessage message = new KafkaMessage();
        message.setProof(null == proof ? (null == kafkaProof.get()
                ? UUID.randomUUID().toString() : kafkaProof.get()) : proof);
        message.setBean(bean);
        if (PlatformConstants.MODE.equalsIgnoreCase(PlatformConstants.MODE_DEV)) {
            message.setOnly(true);
        } else {
            message.setOnly(receive.isOnly());
        }
        message.setGroup(PlatformConstants.GROUP);
        message.setContent(input);
        log.info("[MESSAGE] proof={}, 发送到 {}, 目标 {}", message.getProof(), topics, bean);

        return kafkaTemplate.executeInTransaction(status -> {
            Long index = key;
            if (index <= 0) {
                index = null;
            }

            if (topics.size() > 1) {
                for (String topic : topics) {
                    kafkaTemplate.send(getRecord(topic, index, message));
                }
            } else {
                kafkaTemplate.send(getRecord(topics.get(0), index, message));
            }
            return true;
        });
    }

    private ProducerRecord<Long, KafkaMessage> getRecord(String topic, Long key, KafkaMessage message) {
        final ProducerRecord<Long, KafkaMessage> record = new ProducerRecord<>(topic, key, message);
        record.headers().add(PlatformConstants.PROOF_KEY, message.getProof().getBytes());
        return record;
    }
}
