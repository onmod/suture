package net.dloud.platform.parse.initial;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.parse.setting.DubboSetting;
import net.dloud.platform.parse.utils.RunHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author QuDasheng
 * @create 2018-08-30 17:29
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(name = "dubbo.init.enable", matchIfMissing = true, havingValue = "true")
@AutoConfigureAfter(InitDefault.class)
public class InitDubbo {
    private static final String LOAD_BALANCE = "soft";
    private static final String CLIENT_TYPE = "curator";
    private static final String CACHE_DISABLE = "false";
    private static final String SERIALIZATION = "kryo5";
    private static final String SERVER_TYPE = "netty4";

    @Autowired
    private DubboSetting dubboSetting;

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setId(dubboSetting.getApplication().getId());
        applicationConfig.setName(dubboSetting.getApplication().getName());
        applicationConfig.setLogger("slf4j");
        return applicationConfig;
    }

    @Bean
    public RegistryConfig registryConfig() {
        final DubboSetting.Registry registry = dubboSetting.getRegistry();
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setId(registry.getId());
        registryConfig.setTimeout(registry.getTimeout());
        registryConfig.setProtocol(registry.getProtocol());
        registryConfig.setAddress(registry.getAddress());
        registryConfig.setClient(CLIENT_TYPE);
        registryConfig.setTransporter(SERVER_TYPE);
        return registryConfig;
    }

    @Bean
    public ProtocolConfig protocolConfig() {
        final DubboSetting.Protocol protocol = dubboSetting.getProtocol();
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setId(protocol.getId());
        protocolConfig.setName(protocol.getName());
        protocolConfig.setPort(protocol.getPort());
        protocolConfig.setSerialization(SERIALIZATION);
        //不注册类进来，否则顺序不一样序列化会出错
        //protocolConfig.setOptimizer("net.dloud.platform.parse.dubbo.SerializationOptimizerImpl");
        protocolConfig.setServer(SERVER_TYPE);
        protocolConfig.setTransporter(SERVER_TYPE);
        return protocolConfig;
    }

    @Bean
    public ProviderConfig providerConfig() {
        final DubboSetting.Provider provider = dubboSetting.getProvider();
        ProviderConfig providerConfig = new ProviderConfig();
        if (RunHost.canUseDomain(RunHost.localHost, provider.getHost())) {
            providerConfig.setHost(provider.getHost());
        }
        providerConfig.setThreads(provider.getThreads());
        providerConfig.setTimeout(provider.getTimeout());
        providerConfig.setWeight(provider.getWeight());
        providerConfig.setLoadbalance(LOAD_BALANCE);
        providerConfig.setMock(provider.getMock());
        providerConfig.setAsync(provider.getAsync());
        providerConfig.setFilter("providerFilter");
        providerConfig.setAccesslog(true);
        providerConfig.setDelay(-1);
        return providerConfig;
    }

    @Bean
    public ConsumerConfig consumerConfig() {
        final DubboSetting.Consumer consumer = dubboSetting.getConsumer();
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setTimeout(consumer.getTimeout());
        consumerConfig.setRetries(consumer.getRetries());
        consumerConfig.setLoadbalance(LOAD_BALANCE);
        consumerConfig.setConnections(consumer.getConnections());
        consumerConfig.setCheck(consumer.getCheck());
        if (!CACHE_DISABLE.equalsIgnoreCase(consumer.getCache())) {
            consumerConfig.setCache(consumer.getCache());
        }
        consumerConfig.setAsync(consumer.getAsync());
        consumerConfig.setFilter("consumerFilter");
        consumerConfig.setLazy(true);
        return consumerConfig;
    }
}
