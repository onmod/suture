package net.dloud.platform.common.gateway;

/**
 * @author QuDasheng
 * @create 2018-09-15 23:07
 **/
public interface BaseInjectEnum {
    /**
     * 等级，越大获取的信息越多
     */
    int level();

    /**
     * 获取注解对应的方法
     */
    String method();

    /**
     * 获取默认值
     */
    String acquire();
}
