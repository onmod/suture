package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author QuDasheng
 * @create 2018-09-03 17:02
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassInfo implements Serializable {
    private static final long serialVersionUID = -7719847670002714108L;

    /**
     * 简写
     */
    private String simpleName;

    /**
     * 全名
     */
    private String qualifiedName;

    /**
     * 定义
     */
    private List<String> genericList;

    /**
     * 继承
     */
    private Set<TypeInfo> superclass;

    /**
     * 注解
     */
    private List<AnnotationInfo> annotationInfo;

    /**
     * 导入
     */
    private Set<String> importInfo;

    /**
     * 字段
     */
    private List<FieldInfo> fieldInfo;

    /**
     * 方法
     */
    private List<MethodInfo> methodInfo;

    /**
     * 注释
     */
    private CommentInfo commentInfo;

    /**
     * 是否是成员类
     */
    private Boolean ifMember = false;

    /**
     * 是否是泛型类
     */
    private Boolean ifGeneric = false;

    /**
     * 是否是接口
     */
    private Boolean ifInterface = false;

    /**
     * 类成员是否全部是内部类型
     */
    private Boolean ifPrimitive = true;
}
