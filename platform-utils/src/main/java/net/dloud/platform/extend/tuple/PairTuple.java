package net.dloud.platform.extend.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author QuDasheng
 * @create 2018-09-27 17:34
 **/
@Data
@AllArgsConstructor
public class PairTuple<T, R> {
    private final T first;

    private final R last;
}
