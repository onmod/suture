package net.dloud.platform.common.domain.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.platform.BaseExceptionEnum;

/**
 * @author QuDasheng
 * @create 2018-10-08 01:06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ParamResult<T> extends BaseResult {
    /**
     * 具体内容
     */
    private T result;


    public static <T> ParamResult<T> success(T value) {
        final ParamResult<T> result = new ParamResult<>();
        result.setResult(value);
        return result;
    }

    public static <T> ParamResult<T> failed(String code) {
        final ParamResult<T> result = new ParamResult<>();
        result.setCode(code);
        return result;
    }

    public static <T> ParamResult<T> failed(BaseExceptionEnum exception) {
        return failed(exception.getCode());
    }
}
