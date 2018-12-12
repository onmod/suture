package net.dloud.platform.common.domain.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;

import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-03 14:02
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GatewayMethodResult extends BaseResult {
    /**
     * 要新增或更新的类列表
     */
    private List<String> classList;

    /**
     * 要新增或更新的类信息
     */
    private Map<String, String> classVersion;
}
