package net.dloud.platform.parse.kafka.annotation;

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
public @interface Consumer {
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
     * 失败策略
     */
    String failed() default "";
}
