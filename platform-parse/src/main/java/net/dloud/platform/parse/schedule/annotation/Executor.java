package net.dloud.platform.parse.schedule.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author QuDasheng
 * @create 2018-10-20 00:16
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Executor {
    /**
     * bean名
     */
    @AliasFor(annotation = Component.class)
    String value();

    /**
     * 描述信息
     */
    String describe();

    /**
     * 根据cron表达式执行
     */
    String cron();

    /**
     * 在某个任务之后执行、此时表达式无效
     */
    String after() default "";

    /**
     * 失败时策略
     */
    String failed() default "";

    /**
     * 任务参数
     */
    String jobParam() default "";

    /**
     * 分片数目
     */
    int shardingNum() default 1;

    /**
     * 分片参数
     */
    String shardingParam() default "";
}
