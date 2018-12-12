package net.dloud.platform.common.mapper;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static net.dloud.platform.common.mapper.MapperBuildUtil.COLUMN;
import static net.dloud.platform.common.mapper.MapperBuildUtil.FIELD;
import static net.dloud.platform.common.mapper.MapperBuildUtil.PARAM;
import static net.dloud.platform.common.mapper.MapperBuildUtil.RENEW;
import static net.dloud.platform.common.mapper.MapperBuildUtil.VALUE;
import static net.dloud.platform.common.mapper.MapperBuildUtil.fieldCache;
import static net.dloud.platform.common.mapper.MapperBuildUtil.listString;

/**
 * @author QuDasheng
 * @create 2018-09-13 15:55
 **/
public interface BaseComponent {
    String CREATED_AT = "createdAt";

    String UPDATED_AT = "updatedAt";

    String DELETED_AT = "deletedAt";

    Logger log = LoggerFactory.getLogger(BaseComponent.class);

    default String fields(Class<?> clazz) {
        return listString(fieldCache(clazz), FIELD, null, 0);
    }

    default String fieldsBase(Class<?> clazz) {
        return listString(fieldCache(clazz, false), FIELD, null, 0);
    }

    default String fieldsRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz), FIELD, set, 2);
    }

    default String params(Class<?> clazz) {
        return listString(fieldCache(clazz), PARAM, null, 0);
    }

    default String paramsBase(Class<?> clazz) {
        return listString(fieldCache(clazz, false), PARAM, null, 0);
    }

    default String paramsRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz), PARAM, set, 2);
    }

    default String paramsBaseRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz, false), PARAM, set, 2);
    }

    default String columns(Class<?> clazz) {
        return listString(fieldCache(clazz), COLUMN, null, 0);
    }

    default String columnsBase(Class<?> clazz) {
        return listString(fieldCache(clazz, false), COLUMN, null, 0);
    }

    default String columnsRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz), COLUMN, set, 2);
    }

    default String columnsBaseRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz, false), COLUMN, set, 2);
    }

    default String updates(Class<?> clazz) {
        return listString(fieldCache(clazz), RENEW, null, 0);
    }

    default String updatesRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz), RENEW, set, 2);
    }

    default String values(Class<?> clazz) {
        return listString(fieldCache(clazz), VALUE, null, 0);
    }

    default String valuesRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz), VALUE, set, 2);
    }

    default String valuesBaseRemove(Class<?> clazz, Set<String> set) {
        return listString(fieldCache(clazz, false), VALUE, set, 2);
    }

    default Set<String> keys(String... keys) {
        return Sets.newHashSet(keys);
    }

    default int batchNum(int[] num) {
        int res = 0;
        for (int i = 0; i < num.length; i++) {
            if (num[i] != 0) {
                res++;
            } else {
                log.warn("[DATABASE] 第 {} 条保存失败", i);
            }
        }
        return res;
    }
}


