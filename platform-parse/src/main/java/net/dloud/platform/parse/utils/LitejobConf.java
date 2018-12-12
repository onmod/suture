package net.dloud.platform.parse.utils;

import io.elasticjob.lite.api.simple.SimpleJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.parse.schedule.annotation.Executor;

/**
 * @author QuDasheng
 * @create 2018-10-28 16:03
 **/
public class LitejobConf {

    /**
     * 获取简单任务配置
     */
    public static LiteJobConfiguration simpleJob(final Executor executor, final Class<? extends SimpleJob> jobClass) {
        final JobCoreConfiguration.Builder builder = JobCoreConfiguration.newBuilder(executor.value(), executor.cron(), executor.shardingNum());
        if (StringUtil.notBlank(executor.jobParam())) {
            builder.jobParameter(executor.jobParam());
        }
        if (StringUtil.notBlank(executor.shardingParam())) {
            builder.shardingItemParameters(executor.shardingParam());
        }
        return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(builder.description(executor.describe()).build(),
                jobClass.getCanonicalName())).overwrite(true).build();
    }
}
