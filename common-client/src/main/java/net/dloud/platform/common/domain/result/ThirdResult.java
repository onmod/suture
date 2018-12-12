package net.dloud.platform.common.domain.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;

/**
 * @author QuDasheng
 * @create 2018-09-27 17:34
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ThirdResult<T, M, R> extends BaseResult {
    private T first;

    private M middle;

    private R last;
}