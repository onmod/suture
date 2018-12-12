package net.dloud.platform.parse.kafka;

import lombok.Data;

/**
 * @author QuDasheng
 * @create 2018-10-22 10:18
 **/
@Data
public final class SimpleMessage {
    /**
     * 获取要执行的bean名，必须继承 KafkaConsumer
     */
    private String bean;

    /**
     * 是否允许不同组消费
     */
    private boolean only = false;

    /**
     * 保证顺序时候可以使用key
     */
    private long key = 0L;

    /**
     * 消息正文
     */
    private Object input;


    private SimpleMessage() {
    }

    public static SimpleMessage build(String bean, Object input) {
        final SimpleMessage message = new SimpleMessage();
        message.setBean(bean);
        message.setInput(input);
        return message;
    }

    public static SimpleMessage build(String bean, long key, Object input) {
        final SimpleMessage message = build(bean, input);
        message.setKey(key);
        return message;
    }

    public static SimpleMessage build(String bean, boolean only, long key, Object input) {
        final SimpleMessage message = build(bean, input);
        message.setOnly(only);
        message.setKey(key);
        return message;
    }
}
