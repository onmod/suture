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
public class GroupInfo implements Serializable {
    private static final long serialVersionUID = 8001208743354855181L;

    /**
     * 系统id
     */
    private Integer systemId;

    /**
     * 系统名
     */
    private String systemName;

    /**
     * 运行模式
     */
    private String runMode;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 当前ip
     */
    private String currentIp;

    /**
     * 版本名
     */
    private String versionInfo;
}
