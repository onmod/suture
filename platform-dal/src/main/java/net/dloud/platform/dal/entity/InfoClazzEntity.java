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
@ToString(callSuper = true)
public class InfoClazzEntity extends AbstractEntity {
    private String groupName;

    private String fullName;

    private Integer systemId;

    private Boolean isGeneric = false;

    private Boolean isInterface = false;

    private Boolean isPrimitive = false;

    private String simpleName;

    private String simpleComment;

    private byte[] fieldInfo;

    private byte[] genericInfo;

    private byte[] superclassInfo;

    private byte[] importInfo;

    private byte[] commentInfo;

    private String versionInfo;
}

