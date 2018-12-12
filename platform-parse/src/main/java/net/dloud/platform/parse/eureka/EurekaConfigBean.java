package net.dloud.platform.parse.eureka;

import net.dloud.platform.parse.utils.RunHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author QuDasheng
 * @create 2018-09-02 18:45
 **/
@Primary
@Component
@ConditionalOnClass(value = org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean.class)
public class EurekaConfigBean extends EurekaInstanceConfigBean {
    @Value("${eureka.instance.hostname}")
    private String hostname;

    public EurekaConfigBean(InetUtils inetUtils) {
        super(inetUtils);
    }

    @Override
    public void setInstanceId(String instanceId) {
        super.setInstanceId(instanceId.toLowerCase());
    }

    @Override
    public void setPreferIpAddress(boolean preferIpAddress) {
        // getHostname() 不能使用
        if (RunHost.canUseDomain(RunHost.localHost, hostname)) {
            super.setPreferIpAddress(false);
        } else {
            super.setPreferIpAddress(true);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
