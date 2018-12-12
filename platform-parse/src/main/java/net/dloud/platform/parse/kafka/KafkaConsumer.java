package net.dloud.platform.parse.kafka;

import net.dloud.platform.common.domain.BaseResult;

/**
 * @author QuDasheng
 * @create 2018-10-11 09:58
 **/
@FunctionalInterface
public interface KafkaConsumer<T> {
    /**
     * 消费消息
     */
    BaseResult onMessage(T message);
}
