package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @author QuDasheng
 * @create 2018-09-27 19:22
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionInfo implements Serializable {
    private static final long serialVersionUID = -3650036227075445950L;

    /**
     * 是否允许网关调用 优先级最高
     */
    private Boolean gateway = true;

    /**
     * 允许的来源
     */
    private Set<String> allow;

    /**
     * 不允许的来源
     */
    private Set<String> deny;

    /**
     * 需要的等级 = 优先
     */
    private Integer level = 0;

    /**
     * 需要的最低等级 >=
     */
    private Integer limit = 0;
}
