package net.dloud.platform.common.domain.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseEntry;

/**
 * @author QuDasheng
 * @create 2018-10-08 01:06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PageEntry extends BaseEntry {
    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 要获取的页数
     */
    private Integer pageNum = 1;

    /**
     * 排序方式，1正序，-1倒序
     */
    private Integer sortBy = 1;

    public static PageEntry build(Integer pageSize) {
        if (null == pageSize) {
            return new PageEntry();
        } else {
            final PageEntry entry = new PageEntry();
            entry.setPageSize(pageSize);
            return entry;
        }
    }

    public static PageEntry build(Integer pageSize, Integer pageNum) {
        final PageEntry entry = build(pageSize);
        if (null != pageNum) {
            entry.setPageNum(pageNum);
        }
        return entry;
    }

    public Integer getPageSize() {
        if (null == pageSize) {
            return 10;
        } else {
            return pageSize < 10 ? 10 : pageSize;
        }
    }

    public Integer getPageNum() {
        if (null == pageNum) {
            return 1;
        } else {
            return pageNum < 1 ? 1 : pageNum;
        }
    }

    public Integer getSkipNum() {
        if (null == pageNum || pageNum <= 1) {
            return 0;
        } else {
            return (pageNum - 1) * pageSize;
        }
    }
}
