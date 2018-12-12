package net.dloud.platform.gateway.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.RandomUtil;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.info.FieldDetailInfo;
import net.dloud.platform.common.gateway.info.GenericSimpleInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-04 11:35
 **/
@Slf4j
@SuppressWarnings("unchecked")
public class MockUtil {
    private static final int DEFAULT_STRING_SIZE = 16;

    private static final int MAX_CONTAINER_SIZE = 4;

    private static final int MIN_CONTAINER_SIZE = 2;


    public static Object paramMock(Map<String, Object> input) {
        if (null == input || null == input.get("parameterInfo")) {
            return Collections.emptyMap();
        }

        final List<FieldDetailInfo> parameterInfo = (List) input.get("parameterInfo");

        return parseField(parameterInfo);
    }

    public static Object returnMock(Map<String, Object> input) {
        if (null == input || null == input.get("returnInfo")) {
            return Collections.emptyMap();
        }

        final Object returnObj = input.get("returnInfo");
        if (returnObj instanceof Map) {
            return Collections.emptyMap();
        }

        final FieldDetailInfo returnInfo = (FieldDetailInfo) input.get("returnInfo");

        if (null == returnInfo.getFieldList()) {
            return type2Value(returnInfo.getSimpleTypeName());
        } else {
            return parseField(returnInfo.getFieldList());
        }
    }

    private static Map<String, Object> parseField(List<FieldDetailInfo> fields) {
        if (null == fields) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(fields.size());
        for (FieldDetailInfo field : fields) {
            if (!field.getEnquire()) {
                map.put(field.getFieldName(), type2Value(field));
            }
        }

        return map;
    }

    private static Object type2Value(FieldDetailInfo field) {
        final String simpleTypeName = field.getSimpleTypeName();
        final String fullTypeName = field.getFullTypeName();

        Object value = type2Value(simpleTypeName);
        if (null == value) {
            if (CollectionUtil.isList(fullTypeName)) {
                final int size = RandomUtil.getRandomInt(MIN_CONTAINER_SIZE, MAX_CONTAINER_SIZE);
                GenericSimpleInfo genericType;
                final List<GenericSimpleInfo> genericTypes = field.getGenericTypeName();
                if (null != genericTypes && !genericTypes.isEmpty()) {
                    genericType = genericTypes.get(0);
                } else {
                    genericType = initType();
                }
                setLastType(genericType);

                value = initList(size, field.getGenericTypeDepth(), field, genericType);
            } else if (CollectionUtil.isMap(fullTypeName)) {
                final int size = RandomUtil.getRandomInt(MIN_CONTAINER_SIZE, MAX_CONTAINER_SIZE);
                GenericSimpleInfo keyType;
                GenericSimpleInfo valueType;
                final List<GenericSimpleInfo> genericTypeName = field.getGenericTypeName();
                if (null != genericTypeName && genericTypeName.size() > 1) {
                    keyType = genericTypeName.get(0);
                    valueType = genericTypeName.get(1);
                } else {
                    keyType = initType();
                    valueType = initType();
                }
                setLastType(keyType);
                setLastType(valueType);

                value = initMap(size, field.getGenericTypeDepth(), field, keyType, valueType);
            } else {
                value = parseField(field.getFieldList());
            }
        }

        return value;
    }

    private static GenericSimpleInfo initType() {
        GenericSimpleInfo genericType = new GenericSimpleInfo();
        genericType.setSimpleName("String");
        genericType.setTypeName("java.lang.String");
        return genericType;
    }

    private static void setLastType(GenericSimpleInfo genericType) {
        final String lastTypeName = genericType.getLastTypeName();
        if (null != lastTypeName) {
            genericType.setSimpleName(StringUtil.splitLastByDot(lastTypeName));
            genericType.setTypeName(lastTypeName);
        }
    }

    private static List<Object> initList(int size, int depth, FieldDetailInfo field,
                                         GenericSimpleInfo genericType) {
        final List<Object> list = Lists.newArrayListWithExpectedSize(size);
        for (int j = 0; j < size; j++) {
            if (field.getInnerType()) {
                list.add(type2Value(genericType.getSimpleName()));
            } else {
                list.add(parseField(field.getFieldList()));
            }
        }
        if (depth == 1) {
            return list;
        }

        final List[] lists = new ArrayList[depth];
        lists[0] = list;

        for (int i = 1; i < depth; i++) {
            lists[i] = Lists.newArrayListWithExpectedSize(size);
            for (int j = 0; j < size; j++) {
                lists[i].add(lists[i - 1]);
            }
        }
        return lists[depth - 1];
    }

    private static Map<Object, Object> initMap(int size, int depth, FieldDetailInfo field,
                                               GenericSimpleInfo keyType, GenericSimpleInfo valueType) {
        final Map<Object, Object> map = Maps.newLinkedHashMapWithExpectedSize(size);
        for (int j = 0; j < size; j++) {
            if (field.getInnerType()) {
                map.put(type2Value(keyType.getSimpleName()), type2Value(valueType.getSimpleName()));
            } else {
                map.put(type2Value(keyType.getSimpleName()), parseField(field.getFieldList()));
            }
        }
        if (depth == 1) {
            return map;
        }

        final Map[] maps = new LinkedHashMap[depth];
        maps[0] = map;

        for (int i = 1; i < depth; i++) {
            maps[i] = Maps.newLinkedHashMapWithExpectedSize(size);
            for (int j = 0; j < size; j++) {
                maps[i].put(RandomUtil.getRandomStringByLength(DEFAULT_STRING_SIZE), maps[i - 1]);
            }
        }
        return maps[depth - 1];
    }

    private static Object type2Value(String simpleTypeName) {
        Object value = null;
        if (null == simpleTypeName) {
            return RandomUtil.getRandomStringByLength(DEFAULT_STRING_SIZE);
        }

        simpleTypeName = simpleTypeName.toLowerCase();
        switch (simpleTypeName) {
            case "boolean":
                value = false;
                break;
            case "byte":
                value = RandomUtil.getRandomInt(Byte.MAX_VALUE);
                break;
            case "short":
                value = RandomUtil.getRandomInt(Short.MAX_VALUE);
                break;
            case "int":
            case "integer":
                value = RandomUtil.getRandomInt(Integer.MAX_VALUE);
                break;
            case "long":
                value = RandomUtil.getRandomLong(Long.MAX_VALUE);
                break;
            case "char":
            case "character":
                value = RandomUtil.getRandomStringByLength(1);
                break;
            case "float":
            case "double":
                value = RandomUtil.getRandomDouble(Double.MAX_VALUE);
                break;
            case "string":
                value = RandomUtil.getRandomStringByLength(DEFAULT_STRING_SIZE);
                break;
            case "date":
                value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                break;
            case "timestamp":
                value = System.currentTimeMillis();
                break;
            case "object":
                value = "undefined type";
                break;
        }

        return value;
    }
}
