package net.dloud.platform.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记注入请求信息，不显示到网关
 *
 * @author QuDasheng
 * @create 2018-10-02 08:27
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Enquire {
}
