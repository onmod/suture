package net.dloud.platform.parse.module;

import io.elasticjob.lite.api.simple.SimpleJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.event.JobEventConfiguration;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import io.elasticjob.lite.spring.api.SpringJobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author QuDasheng
 * @create 2018-09-27 16:47
 **/
@Slf4j
@Component
@ConditionalOnProperty(name = "schedule.init.enable", matchIfMissing = true, havingValue = "true")
public class ScheduleComponent {
    @Autowired
    private ZookeeperRegistryCenter regCenter;

    @Autowired
    private JobEventConfiguration jobEventConfiguration;


    /**
     * 添加简单任务
     */
    public void createTask(final SimpleJob simpleJob, final String cron, final int shardingTotalCount,
                           final String shardingItemParameters) {
        new SpringJobScheduler(simpleJob, regCenter,
                simpleJobConfiguration(simpleJob.getClass(), cron, shardingTotalCount, shardingItemParameters),
                jobEventConfiguration).init();
    }

    /**
     * 更新简单任务
     */
    public void updateTask(final String taskName, final String cron) {
        JobRegistry.getInstance().getJobScheduleController(taskName).rescheduleJob(cron);
    }

    /**
     * 停止简单任务
     */
    public void stopTask(final String taskName) {
        JobRegistry.getInstance().getJobScheduleController(taskName).shutdown();
    }

    /**
     * 删除简单任务
     */
    public void deleteTask(final String taskName) {
        JobRegistry.getInstance().getRegCenter(taskName).remove("/" + taskName);
    }

    /**
     * 获取简单任务
     */
    private LiteJobConfiguration simpleJobConfiguration(final Class<? extends SimpleJob> jobClass, final String cron,
                                                        final int shardingTotalCount, final String shardingItemParameters) {
        return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                "beanName", cron, shardingTotalCount).shardingItemParameters(shardingItemParameters).build(),
                jobClass.getCanonicalName())).overwrite(true).build();
    }
}
