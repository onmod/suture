package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author QuDasheng
 * @create 2018-09-11 11:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemInfo implements Serializable {
    private static final long serialVersionUID = 3346770943358722698L;

    /**
     * 系统id
     */
    private Integer systemId;

    /**
     * 系统名
     */
    private String systemName;

    /**
     * 组名
     */
    private String simpleName;
}
