package net.dloud.platform.common.domain.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;
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
public class ListResult<T> extends BaseResult {
    /**
     * 每页具体内容
     */
    private List<T> results;


    public static <T> ListResult<T> success(List<T> values) {
        final ListResult<T> result = new ListResult<>();
        result.setResults(values);
        return result;
    }

    public static <T> ListResult<T> failed(String code) {
        final ListResult<T> result = new ListResult<>();
        result.setCode(code);
        result.setResults(Collections.emptyList());
        return result;
    }

    public static <T> ListResult<T> failed(BaseExceptionEnum exception) {
        return failed(exception.getCode());
    }
}
