package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-09-04 09:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeInfo implements Serializable {
    private static final long serialVersionUID = 7657518138352485545L;

    /**
     * 类型
     */
    private String simpleName;

    /**
     * 展开类型
     */
    private String qualifiedName;

    /**
     * 全展开
     */
    private String fullName;

    /**
     * 范型信息
     */
    private List<TypeInfo> genericInfo;

    /**
     * 是否基本类型
     */
    private Boolean ifPrimitive = false;

    /**
     * 是否是数组
     */
    private Boolean ifArray = false;

    /**
     * 是否有泛型
     */
    private Boolean ifGeneric = false;
}
