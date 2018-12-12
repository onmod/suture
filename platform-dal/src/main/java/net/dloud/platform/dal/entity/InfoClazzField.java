package net.dloud.platform.dal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoClazzField {
    private String groupName;

    private String simpleName;

    private String fullName;

    private byte[] fieldInfo;

    private byte[] genericInfo;

    private byte[] superclassInfo;

    private byte[] importInfo;

    private byte[] commentInfo;

    private Boolean isGeneric = false;

    private Boolean isPrimitive = false;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}

