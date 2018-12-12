package net.dloud.platform.parse.kafka.listener;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.common.domain.result.ExceptionResult;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.parse.kafka.KafkaConsumer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author QuDasheng
 * @create 2018-10-11 09:58
 **/
@Slf4j
@Component
public class SimpleMessageExecutor implements MessageExecutor {
    @Autowired
    private ApplicationContext context;

    /**
     * 消费消息
     */
    @Override
    @SuppressWarnings("unchecked")
    public BaseResult execute(KafkaMessage message) {
        final String proof = message.getProof();
        try {
            final KafkaConsumer consumer = context.getBean(message.getBean(), KafkaConsumer.class);
            final BaseResult result = consumer.onMessage(message.getContent());
            log.info("[MESSAGE] 当前消息消费完毕: proof={}, result={}", proof, result);
            return result;
        } catch (BeansException e) {
            log.warn("[MESSAGE] 当前消息消费方法未找到, 不能进行消费: proof={}, {}, {}", proof, message.getBean(), e.getMessage());
        } catch (Exception e) {
            log.error("[MESSAGE] 当前消息消费失败: proof={}, {}", proof, message.getBean());
            log.error("", e);
        }
        return new ExceptionResult(PlatformExceptionEnum.MQ_CONSUMER_ERROR);
    }
}
