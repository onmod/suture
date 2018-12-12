package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @author QuDasheng
 * @create 2018-10-02 09:25
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InjectionInfo implements Serializable {
    private static final long serialVersionUID = 3933802328241790917L;

    /**
     * 注入参数的类型 InjectEnum 的值，不带点
     */
    private String injectType;

    /**
     * 注入时要调用的方法名
     */
    private String invokeName;

    /**
     * 是否注入ip
     */
    private Boolean haveAddress = false;

    /**
     * header名
     */
    private Set<String> headerNames;

    /**
     * cookie名
     */
    private Set<String> cookieNames;
}
