package net.dloud.platform.common.extend;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author QuDasheng
 * @create 2018-09-16 21:57
 **/
public class LambdaUtil {
    public static <T> Stream<T> streamOrEmpty(Collection<T> collection) {
        if (null == collection || collection.isEmpty()) {
            return Stream.empty();
        } else {
            return collection.stream();
        }
    }
}
