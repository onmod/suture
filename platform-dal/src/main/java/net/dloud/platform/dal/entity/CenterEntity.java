package net.dloud.platform.dal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.AbstractEntity;
import net.dloud.platform.common.domain.result.KeystoreResult;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CenterEntity extends AbstractEntity {
    private Integer systemId;

    private byte[] systemSecret;

    private byte[] sourceInit;

    private Integer invokeLimit = -1;
}

