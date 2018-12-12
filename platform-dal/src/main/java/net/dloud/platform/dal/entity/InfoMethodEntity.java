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
public class InfoMethodEntity extends AbstractEntity {
    private String groupName;

    private Integer systemId;

    private String clazzName;

    private String pathName;

    private String invokeName;

    private Integer invokeLength;

    private Boolean isWhitelist = false;

    private Boolean isBackground = false;

    private String simpleName;

    private byte[] simpleParameter;

    private String simpleComment;

    private byte[] parameterInfo;

    private byte[] returnInfo;

    private byte[] commentInfo;

    private byte[] injectionInfo;

    private byte[] permissionInfo;

    private byte[] methodData;

    private String paramMock;

    private String returnMock;
}

