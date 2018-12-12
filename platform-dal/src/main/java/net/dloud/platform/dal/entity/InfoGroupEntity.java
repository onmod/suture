package net.dloud.platform.dal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.AbstractEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "currentIp")
public class InfoGroupEntity extends AbstractEntity {
    private String groupName;

    private Integer systemId;

    private String systemName;

    private byte[] currentIp;

    private String versionInfo;
}

