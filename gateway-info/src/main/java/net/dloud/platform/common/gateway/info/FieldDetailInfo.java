package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-09-16 21:27
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldDetailInfo implements Serializable {
    private static final long serialVersionUID = 7752327430277052902L;

    /**
     * 参数名
     */
    private String fieldName;

    /**
     * 参数类型-简
     */
    private String simpleTypeName;

    /**
     * 参数类型-全
     */
    private String fullTypeName;

    /**
     * 是否是内部类
     */
    private Boolean innerType = false;

    /**
     * 是否是注入字段，是则不显示到网关
     */
    private Boolean enquire = false;

    /**
     * 范型信息
     */
    private List<GenericSimpleInfo> genericTypeName;

    /**
     * 范型深度
     */
    private Integer genericTypeDepth = 0;

    /**
     * 非基本类型字段信息
     */
    private List<FieldDetailInfo> fieldList;

    /**
     * 作为方法参数的注释
     */
    private String simpleComment;

    /**
     * 作为类的注释
     */
    private String extendComment;
}
