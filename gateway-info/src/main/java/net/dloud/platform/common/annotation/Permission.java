package net.dloud.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于权限校验
 * JDT解析的注解不能出现 {} 否则读取不到注释, 所以下边多个系统使用 , 号分割
 *
 * @author QuDasheng
 * @create 2018-09-27 19:13
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Permission {
    /**
     * 是否允许网关调用 优先级最高
     */
    boolean value() default true;

    /**
     * 不允许的来源 优先
     */
    String deny() default "";

    /**
     * 允许的来源
     */
    String allow() default "*";

    /**
     * 需要的等级 = 优先
     */
    int level() default -1;

    /**
     * 需要的最低等级 >=
     */
    int limit() default 1;
}
