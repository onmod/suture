package net.dloud.platform.common.domain;

import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author QuDasheng
 * @create 2018-09-13 13:38
 **/
@Data
public abstract class AbstractEntity {
    private Timestamp createdAt = Timestamp.from(Instant.now());

    private Timestamp updatedAt = Timestamp.from(Instant.now());

    private Timestamp deletedAt;
}
