package net.dloud.platform.parse.kafka.listener;

import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.domain.message.KafkaMessage;

/**
 * @author QuDasheng
 * @create 2018-10-15 13:49
 **/
public interface MessageExecutor {
    /**
     * 实际消费消息
     */
    BaseResult execute(KafkaMessage message);
}
