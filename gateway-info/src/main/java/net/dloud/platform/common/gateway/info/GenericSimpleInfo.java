package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-10-01 12:57
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericSimpleInfo {
    /**
     * 范型简名
     */
    private String simpleName;

    /**
     * 范型类型名
     */
    private String typeName;

    /**
     * 下一级类型
     */
    private List<GenericSimpleInfo> subTypeName;

    /**
     * 最里层范型类型名，只保留一个
     */
    private String lastTypeName;
}
