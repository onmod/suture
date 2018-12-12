package net.dloud.platform.common.domain.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author QuDasheng
 * @create 2018-10-08 01:06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PageSinceEntry extends PageEntry {
    /**
     * 在某个id之前或之后
     */
    private Long sinceId;
}
