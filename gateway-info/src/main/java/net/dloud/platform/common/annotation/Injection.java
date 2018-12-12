package net.dloud.platform.common.annotation;

import net.dloud.platform.common.gateway.InjectEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于网关用户注入
 * JDT解析的注解不能出现 {} 否则读取不到注释, 所以下边多个header或者cookie使用 , 号分割
 *
 * @author QuDasheng
 * @create 2018-09-14 09:43
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Injection {
    /**
     * 注入类型，具体格式参照 BaseInjectEnum 的实现类
     */
    InjectEnum value() default InjectEnum.MEMBER_ID;

    /**
     * 请求IP，当 InjectEnum 不是默认值时无效
     * 要求接收对象是 map 或者 bean 包含 requestIp(Integer) 参数
     */
    boolean ip() default false;

    /**
     * 请求头，当 InjectEnum 不是默认值时无效
     * 要求接收对象是 map 或者 bean 包含 requestHeaders(Map<String, String>) 参数
     */
    String headers() default "";

    /**
     * 请求头，当 InjectEnum 不是默认值时无效
     * 要求接收对象是 map 或者 bean 包含 requestCookies(Map<String, byte[]>) 参数
     */
    String cookies() default "";
}
