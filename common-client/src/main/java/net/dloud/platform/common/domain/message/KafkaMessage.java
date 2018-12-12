package net.dloud.platform.common.domain.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseMessage;

/**
 * @author QuDasheng
 * @create 2018-10-12 10:31
 **/
@Data
@ToString(callSuper = true, exclude = {"content"})
@EqualsAndHashCode(callSuper = true)
public class KafkaMessage extends BaseMessage {
    /**
     * 消息正文
     */
    private Object content;
}
