package net.dloud.platform.parse.initial;

import io.elasticjob.lite.api.simple.SimpleJob;
import io.elasticjob.lite.event.JobEventConfiguration;
import io.elasticjob.lite.event.rdb.JobEventRdbConfiguration;
import io.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import io.elasticjob.lite.spring.api.SpringJobScheduler;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.schedule.annotation.Executor;
import net.dloud.platform.parse.utils.LitejobConf;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-07 17:23
 **/
@Slf4j
@EnableAsync
@Configuration
@ConditionalOnProperty(name = "schedule.init.enable", matchIfMissing = true, havingValue = "true")
@AutoConfigureAfter(InitSource.class)
public class InitSchedule {

    @Bean
    public ZookeeperRegistryCenter zookeeperRegistryCenter() {
        final ZookeeperConfiguration conf = new ZookeeperConfiguration(PlatformConstants.ZK_ADDRESS,
                "elastic_job/" + PlatformConstants.APPID);
        ZookeeperRegistryCenter registryCenter = new ZookeeperRegistryCenter(conf);
        registryCenter.init();
        return registryCenter;
    }

    @Bean
    public JobEventConfiguration jobEventConfiguration(@Qualifier("pubDataSource") DataSource dataSource) {
        return new JobEventRdbConfiguration(dataSource);
    }

    @Bean("scheduleListener")
    public ApplicationListener<ContextRefreshedEvent> scheduleListener(ApplicationContext context,
                                                                       ZookeeperRegistryCenter registryCenter,
                                                                       JobEventConfiguration jobEventConfiguration) {
        return (event) -> {
            try {
                final Map<String, Object> jobMap = context.getBeansWithAnnotation(Executor.class);
                try {
                    for (String beanName : jobMap.keySet()) {
                        final SimpleJob job = (SimpleJob) jobMap.get(beanName);
                        final Executor executor = job.getClass().getAnnotation(Executor.class);
                        final SpringJobScheduler scheduler = new SpringJobScheduler(
                                job, registryCenter, LitejobConf.simpleJob(executor, job.getClass()), jobEventConfiguration);
                        scheduler.init();
                    }
                } catch (ClassCastException e) {
                    log.warn("[PLATFORM] 定时任务转换失败, 请确认其类型: ", e);
                }
                log.info("[PLATFORM] 当前初始化了任务: {}", jobMap.keySet());
            } catch (Exception e) {
                log.warn("[PLATFORM] 初始化任务失败: ", e);
            }
        };
    }
}
