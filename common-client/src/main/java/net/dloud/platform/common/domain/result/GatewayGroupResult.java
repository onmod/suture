package net.dloud.platform.common.domain.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;

/**
 * @author QuDasheng
 * @create 2018-09-03 14:02
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GatewayGroupResult extends BaseResult {
    /**
     * 是否是新组
     */
    private boolean newgroup = false;

    /**
     * 是否一致，一致则进行下一步更新
     */
    private boolean consistent = false;
}
