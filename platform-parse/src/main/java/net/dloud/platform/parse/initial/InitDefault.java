package net.dloud.platform.parse.initial;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.curator.wrapper.CuratorWrapper;
import net.dloud.platform.parse.dubbo.wrapper.DubboWrapper;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.DUBBO_GROUP_PATH;

/**
 * @author QuDasheng
 * @create 2018-10-03 15:17
 **/
@Slf4j
@Configuration
public class InitDefault {
    private static CuratorFramework curatorClient;

    @PostConstruct
    private void init() {
        curatorClient = CuratorWrapper.initClient();
    }

    @PreDestroy
    private void destroy() {
        curatorClient.close();
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(PlatformConstants.PROCESSOR_NUMBER * 2);
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Bean("dubboListener")
    public ApplicationListener<ContextRefreshedEvent> dubboListener() {
        return (event) -> {
            final String currentPath = DubboWrapper.currentPath();
            log.info("[PLATFORM] DUBBO初始化GROUP使用地址: {}", currentPath);

            try {
                CuratorWrapper.addListeners(() -> CuratorWrapper.childrenCache(DubboWrapper.dubboListener
                        (PlatformConstants.GROUP, DUBBO_GROUP_PATH), currentPath, DUBBO_GROUP_PATH));
            } catch (Exception e) {
                log.error("[PLATFORM] DUBBO初始化GROUP失败: {}, {}", currentPath, e.getMessage());
            }
        };
    }
}
