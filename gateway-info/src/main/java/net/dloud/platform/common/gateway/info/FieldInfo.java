package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-09-09 18:08
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldInfo implements Serializable {
    private static final long serialVersionUID = 6859594426954976109L;

    /**
     * 名字
     */
    private String fieldName;

    /**
     * 类型
     */
    private TypeInfo typeInfo;

    /**
     * 注解
     */
    private List<AnnotationInfo> annotationInfo;

    /**
     * 注释
     */
    private CommentInfo commentInfo;

    /**
     * 是否是查询参数，是则不显示到网关
     */
    private Boolean ifEnquire = false;
}
