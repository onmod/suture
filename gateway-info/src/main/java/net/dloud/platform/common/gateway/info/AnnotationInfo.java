package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-03 17:02
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnotationInfo implements Serializable {
    private static final long serialVersionUID = -6390825357718671741L;

    /**
     * 简写
     */
    private String simpleName;

    /**
     * 全名
     */
    private String qualifiedName;

    /**
     * 注解值
     */
    private Map<String, Object> annotationValue;
}
