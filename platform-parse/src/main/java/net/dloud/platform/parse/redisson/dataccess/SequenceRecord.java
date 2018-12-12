package net.dloud.platform.parse.redisson.dataccess;

import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author QuDasheng
 * @create 2018-10-26 23:52
 **/
@Data
public class SequenceRecord {
    private Long start;

    private Long finish;

    private Byte incType = 1;

    private Integer refresh = 1000;

    private Byte step = 10;

    private Long assist = 0L;

    private Timestamp updatedAt = Timestamp.from(Instant.now());


    public SequenceRecord() {
    }

    public SequenceRecord(Long start) {
        this.start = start;
        this.finish = start + this.refresh;
    }
}
