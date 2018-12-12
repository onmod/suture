package net.dloud.platform.parse.kafka.listener;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static net.dloud.platform.parse.utils.SourceGet.kafkaProof;

/**
 * @author QuDasheng
 * @create 2018-10-11 17:36
 **/
@Slf4j
@Component
public final class KafkaMessageListener implements AcknowledgingMessageListener<Long, KafkaMessage> {

    @Autowired
    private MessageExecutor messageExecutor;

    @Override
    public void onMessage(ConsumerRecord<Long, KafkaMessage> data, Acknowledgment acknowledgment) {
        final Long key = data.key();
        final KafkaMessage message = data.value();
        log.info("[MESSAGE] 接收到的消息 topic = {} | partition = {} | {} = {}",
                data.topic(), data.partition(), key, message);

        if (null == message) {
            log.warn("[MESSAGE] 当前消息为空, 不进行消费");
            return;
        }

        final String proof;
        final Iterable<Header> iterable = data.headers().headers(PlatformConstants.PROOF_KEY);
        if (null != iterable && iterable.iterator().hasNext()) {
            proof = new String(iterable.iterator().next().value());
        } else {
            proof = message.getProof();
        }
        if (StringUtil.isBlank(kafkaProof.get()) || !proof.equalsIgnoreCase(kafkaProof.get())) {
            kafkaProof.set(proof);
        }

        final String messageBean = message.getBean();
        if (null == messageBean) {
            log.info("[MESSAGE] 当前消息消费方法为空, 不进行消费: proof={}", proof);
            return;
        }
        if (null == message.getContent()) {
            log.info("[MESSAGE] 当前消息内容为空, 不进行消费: proof={}", proof);
            return;
        }
        if (message.getOnly() && !PlatformConstants.GROUP.equalsIgnoreCase(message.getGroup())) {
            log.info("[MESSAGE] 当前消息设置为不消费: proof={}, {}", proof, message.getGroup());
            return;
        }

        try {
            // 处理接收到的消息
            messageExecutor.execute(message);
            log.info("[MESSAGE] 消费完成开始提交offset: proof={}", proof);
            acknowledgment.acknowledge();
            log.info("[MESSAGE] 当前消息offset提交成功: proof={}", proof);
        } catch (Exception e) {
            log.error("[MESSAGE] 消息消费过程中出现异常: ", e);
        }
    }
}
