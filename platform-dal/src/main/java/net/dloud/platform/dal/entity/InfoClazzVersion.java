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
public class InfoClazzVersion {
    private String simpleName;

    private String fullName;

    private String versionInfo;
}
