package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-09-03 17:08
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterInfo implements Serializable {
    private static final long serialVersionUID = -4017468184731847618L;

    /**
     * 参数名
     */
    private String paramName;

    /**
     * 参数类型
     */
    private TypeInfo paramType;

    /**
     * 注解
     */
    private List<AnnotationInfo> annotationInfo;

    /**
     * 是否需要注入
     */
    private Boolean ifInjection = false;

    /**
     * 具体需要注入的参数
     */
    private InjectionInfo injectionInfo;
}
