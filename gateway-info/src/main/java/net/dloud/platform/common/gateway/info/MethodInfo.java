package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-03 17:02
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodInfo implements Serializable {
    private static final long serialVersionUID = -4193728371125475942L;

    /**
     * 简写
     */
    private String simpleName;

    /**
     * 调用名
     */
    private String invokeName;

    /**
     * 注解
     */
    private List<AnnotationInfo> annotationInfo;

    /**
     * 输入类型
     */
    private Map<String, ParameterInfo> parameterInfo;

    /**
     * 返回类型
     */
    private TypeInfo returnInfo;

    /**
     * 注释
     */
    private CommentInfo commentInfo;

    /**
     * 权限
     */
    private PermissionInfo permissionInfo;

    /**
     * 是否在白名单中
     */
    private Boolean ifWhitelist = false;

    /**
     * 是否在是后台接口
     */
    private Boolean ifBackground = false;
}
