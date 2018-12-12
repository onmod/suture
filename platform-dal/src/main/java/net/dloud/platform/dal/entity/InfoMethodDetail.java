package net.dloud.platform.dal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoMethodDetail {
    private String clazzName;

    private String pathName;

    private String invokeName;

    private int invokeLength;

    private boolean isWhitelist;

    private boolean isBackground;

    private byte[] parameterInfo;

    private byte[] returnInfo;

    private byte[] commentInfo;

    private byte[] injectionInfo;

    private byte[] permissionInfo;

    private byte[] methodData;

    private String paramMock;

    private String returnMock;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}

