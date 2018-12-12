package net.dloud.platform.parse.setting;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author QuDasheng
 * @create 2018-08-30 17:39
 **/
@Data
@Component
@ConfigurationProperties(prefix = "dubbo")
public class DubboSetting {
    private Application application;

    private Protocol protocol;

    private Registry registry;

    private Consumer consumer;

    private Provider provider;


    @Data
    public static class Application {
        private String id;

        private String name;
    }

    @Data
    public static class Protocol {
        private String id;

        private String name;

        private Integer port;
    }

    @Data
    public static class Registry {
        private String id;

        private String protocol;

        private String address;

        private Integer timeout;
    }

    @Data
    public static class Provider {
        private String host;

        private Integer threads;

        private Integer timeout;

        private Integer weight;

        private String loadbalance;

        private Boolean mock;

        private Boolean async;

    }

    @Data
    public static class Consumer {
        private Integer timeout;

        private Integer retries;

        private Integer connections;

        private String loadbalance;

        private Boolean check;

        private String cache;

        private Boolean cacheEnable;

        private Boolean async;
    }
}
