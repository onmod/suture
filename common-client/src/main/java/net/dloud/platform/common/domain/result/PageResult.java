package net.dloud.platform.common.domain.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.domain.entry.PageEntry;
import net.dloud.platform.common.platform.BaseExceptionEnum;

import java.util.Collections;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-10-08 01:06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends BaseResult {
    /**
     * 总条数
     */
    private Long totalNum = 0L;

    /**
     * 总页数
     */
    private Long totalPageNum = 1L;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 每页具体内容
     */
    private List<T> results;


    public static <T> PageResult<T> build(PageEntry pageEntry, Long totalNum) {
        final PageResult<T> result = new PageResult<>();
        if (null != pageEntry && totalNum > 0) {
            Integer pageSize = pageEntry.getPageSize();
            Long totalPageNum = totalNum / pageSize;
            if (totalNum % pageSize != 0) {
                totalPageNum += 1;
            }
            result.setTotalPageNum(totalPageNum);
            result.setTotalNum(totalNum);
            result.setPageSize(pageSize);
        }
        return result;
    }

    public static <T> PageResult<T> build(PageEntry pageEntry, Long totalNum, List<T> values) {
        final PageResult<T> result = build(pageEntry, totalNum);
        result.setResults(values);
        return result;
    }

    public static <T> PageResult<T> failed(String code) {
        final PageResult<T> result = new PageResult<>();
        result.setCode(code);
        result.setResults(Collections.emptyList());
        return result;
    }

    public static <T> PageResult<T> failed(BaseExceptionEnum exception) {
        return failed(exception.getCode());
    }
}
