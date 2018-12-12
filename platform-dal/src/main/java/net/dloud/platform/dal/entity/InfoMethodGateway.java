package net.dloud.platform.dal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author QuDasheng
 * @create 2018-09-11 14:40
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoMethodGateway {
    private String clazzName;

    private Boolean isWhitelist;

    private Boolean isBackground;

    private String simpleName;

    private byte[] simpleParameter;

    private byte[] injectionInfo;

    private byte[] permissionInfo;
}
