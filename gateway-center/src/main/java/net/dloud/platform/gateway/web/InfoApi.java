package net.dloud.platform.gateway.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.InjectEnum;
import net.dloud.platform.common.gateway.info.CommentInfo;
import net.dloud.platform.common.gateway.info.FieldDetailInfo;
import net.dloud.platform.common.gateway.info.GenericSimpleInfo;
import net.dloud.platform.common.gateway.info.InjectionInfo;
import net.dloud.platform.common.serialize.Beans;
import net.dloud.platform.common.serialize.InnerTypeUtil;
import net.dloud.platform.common.serialize.Jsons;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.dal.InfoComponent;
import net.dloud.platform.dal.entity.InfoClazzField;
import net.dloud.platform.dal.entity.InfoClazzSimple;
import net.dloud.platform.dal.entity.InfoMethodDetail;
import net.dloud.platform.dal.entity.InfoMethodSimple;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.gateway.util.MockUtil;
import net.dloud.platform.gateway.util.ResultWrapper;
import net.dloud.platform.parse.dubbo.wrapper.DubboWrapper;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author QuDasheng
 * @create 2018-09-10 20:20
 **/
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/info")
@ConditionalOnProperty(value = "run.mode", havingValue = "dev")
public class InfoApi {
    private static final Jsons jsons = Jsons.getDefault();
    private static final Beans beans = Beans.getDefault();

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private InfoComponent infoComponent;

    @RequestMapping(value = "/dubbo_provider")
    public Mono<Map<String, Set<String>>> dubbo_provider() {
        return Mono.just(DubboWrapper.dubboProvider);
    }

    @RequestMapping(value = "/group_list")
    public Flux<Map<String, Object>> group_list(String group) {
        return Flux.fromStream(
                jdbi.withHandle(handle -> infoComponent.findGroup(handle, checkGroup(group))).stream().map(one -> {
                    final Map<String, Object> map = beans.bean2Map(one);
                    map.put("currentIp", KryoBaseUtil.readFromByteArray(one.getCurrentIp()));
                    return map;
                }));
    }

    @RequestMapping(value = "/clazz_list")
    public Flux<Map<String, Object>> clazz_list(String group, Integer system) {
        AssertWrapper.notNull(system, "必须输入调用系统");

        return Flux.fromStream(() -> {
            final List<InfoClazzSimple> simpleClazz = jdbi.withHandle(handle ->
                    infoComponent.findSimpleClazz(handle, checkGroup(group), system, true));
            log.info("[GATEWAY] 查询的方法: {}", simpleClazz);

            List<String> clazzNames = simpleClazz.stream().map(InfoClazzSimple::getFullName)
                    .distinct().collect(Collectors.toList());

            if (clazzNames.isEmpty()) {
                return Stream.empty();
            } else {
                final Map<String, List<InfoMethodSimple>> simpleMethod = jdbi.withHandle(handle ->
                        infoComponent.findSimpleMethod(handle, checkGroup(group), clazzNames))
                        .stream().collect(Collectors.groupingBy(InfoMethodSimple::getClazzName));

                return simpleClazz.stream().map(one -> {
                    final Map<String, Object> map = beans.bean2Map(one);
                    map.remove("clazzName");
                    map.put("methodInfo", simpleMethod.getOrDefault(one.getFullName(), Collections.emptyList()));
                    return map;
                });
            }
        });
    }

    @RequestMapping(value = "/method_detail")
    public Mono<Map<String, Object>> method_detail(String group, Integer system, String invokeName, Integer invokeLength) {
        AssertWrapper.notNull(system, "必须输入调用系统");

        return Mono.fromSupplier(() -> {
            final String checkGroup = checkGroup(group);

            return jdbi.withHandle(handle -> {
                final InfoMethodDetail methodDetail = infoComponent.getMethodDetail(handle, checkGroup, invokeName, invokeLength)
                        .orElseThrow(() -> new PassedException(PlatformExceptionEnum.NOT_FOUND));
                log.info("[GATEWAY] 查询的方法 {} ({}) | [{}] | [{}]", invokeName, invokeLength, system, checkGroup);

                if (null != methodDetail.getMethodData()) {
                    Map<String, Object> map = KryoBaseUtil.readFromByteArray(methodDetail.getMethodData());

                    boolean update = false;
                    String paramMock = methodDetail.getParamMock();
                    if (null == paramMock) {
                        update = true;
                        paramMock = jsons.toJson(MockUtil.paramMock(map));
                    }
                    String returnMock = methodDetail.getReturnMock();
                    if (null == returnMock) {
                        update = true;
                        returnMock = jsons.toJson(MockUtil.returnMock(map));
                    }
                    map.remove("methodData");

                    if (update) {
                        infoComponent.updateMethodCache(handle, checkGroup, invokeName, invokeLength,
                                methodDetail.getMethodData(), paramMock, returnMock);
                    }
                    //注意顺序
                    map.put("paramMock", paramMock);
                    map.put("returnMock", returnMock);
                    return map;
                } else {
                    Map<String, Object> map = beans.bean2Map(methodDetail);
                    if (null != methodDetail.getCommentInfo()) {
                        map.put("commentInfo", decodeColumn(methodDetail.getCommentInfo()));
                    } else {
                        map.put("commentInfo", Collections.emptyMap());
                    }
                    if (null != methodDetail.getInjectionInfo()) {
                        map.put("injectionInfo", decodeColumn(methodDetail.getInjectionInfo()));
                    } else {
                        map.put("permissionInfo", Collections.emptyMap());
                    }
                    if (null != methodDetail.getPermissionInfo()) {
                        map.put("permissionInfo", decodeColumn(methodDetail.getPermissionInfo()));
                    } else {
                        map.put("permissionInfo", Collections.emptyMap());
                    }


                    //开始处理参数
                    Set<String> clazzNames = Sets.newHashSet(methodDetail.getClazzName());
                    final byte[] parameterBytes = methodDetail.getParameterInfo();
                    List<FieldDetailInfo> parameterInfo = null;
                    if (null != parameterBytes) {
                        parameterInfo = decodeColumn(parameterBytes);
                        clazzNames.addAll(parameterInfo.stream().map(FieldDetailInfo::getFullTypeName).collect(Collectors.toSet()));
                    } else {
                        map.put("parameterInfo", Collections.emptyList());
                    }

                    final byte[] returnBytes = methodDetail.getReturnInfo();
                    FieldDetailInfo returnInfo = null;
                    if (null != returnBytes) {
                        returnInfo = decodeColumn(returnBytes);
                        clazzNames.add(returnInfo.getFullTypeName());
                    } else {
                        map.put("parameterInfo", Collections.emptyMap());
                    }

                    if (!clazzNames.isEmpty()) {
                        clazzNames.removeAll(InnerTypeUtil.innerClass.values());
                        if (!clazzNames.isEmpty()) {
                            //其它引用的类名
                            final Map<String, Optional<InfoClazzField>> refClazzField = getField(checkGroup, clazzNames);
                            final Set<String> refClazzNames = Sets.newHashSet();
                            //导入引用的数据
                            checkClazzName(refClazzField, refClazzNames, parameterInfo, returnInfo);
                            if (!refClazzNames.isEmpty()) {
                                refClazzField.putAll(getField(checkGroup, refClazzNames));
                            }

                            if (null != parameterInfo) {
                                //抹去注入的参数
                                final Map<String, String> injectionNames = Maps.newHashMap();
                                if (null != methodDetail.getInjectionInfo()) {
                                    final Map<String, InjectionInfo> injectionInfo = KryoBaseUtil.readFromByteArray(methodDetail.getInjectionInfo());
                                    injectionInfo.forEach((key, info) -> {
                                        if (null != info && null != info.getInjectType()) {
                                            injectionNames.put(key, StringUtil.splitLastByDot(info.getInjectType()));
                                        }
                                    });
                                }
                                if (injectionNames.isEmpty()) {
                                    parameterInfo.forEach(parameterOne -> clazzType(parameterOne, refClazzField));
                                } else {
                                    parameterInfo = parameterInfo.stream().filter(one -> {
                                        final String fieldName = one.getFieldName();
                                        final String injectionType = injectionNames.get(fieldName);
                                        //字段名有注入信息，并且当是 MEMBER_ID 时类型是 Long
                                        final boolean enquire = injectionNames.keySet().contains(fieldName)
                                                && (!injectionType.equalsIgnoreCase(InjectEnum.MEMBER_ID.toString())
                                                || InnerTypeUtil.isLongType(one.getFullTypeName()));
                                        one.setEnquire(enquire);
                                        return !enquire;
                                    }).collect(Collectors.toList());
                                    parameterInfo.forEach(parameterOne -> clazzType(parameterOne, refClazzField));
                                }
                                map.put("parameterInfo", parameterInfo);
                            }
                            if (null != returnInfo) {
                                clazzType(returnInfo, refClazzField);
                                map.put("returnInfo", returnInfo);
                            }
                        } else {
                            if (null != parameterInfo) {
                                map.put("parameterInfo", parameterInfo);
                            }
                            if (null != returnInfo) {
                                map.put("returnInfo", returnInfo);
                            }
                        }
                    }

                    String paramMock = jsons.toJson(MockUtil.paramMock(map));
                    String returnMock = jsons.toJson(MockUtil.returnMock(map));
                    map.remove("methodData");

                    final int result = infoComponent.updateMethodCache(handle, checkGroup, invokeName, invokeLength,
                            KryoBaseUtil.writeToByteArray(map), paramMock, returnMock);
                    log.info("[GATEWAY] 方法缓存更新结果: {}", result);

                    //注意顺序
                    map.put("paramMock", paramMock);
                    map.put("returnMock", returnMock);
                    return map;
                }
            });
        });
    }

    @RequestMapping(value = "/method_mock")
    public Mono<ApiResponse> method_mock(String group, Integer system, String invokeName, Integer invokeLength) {
        AssertWrapper.notNull(system, "必须输入调用系统");
        log.info("[GATEWAY] 查询的方法 {} ({}) | [{}] | [{}]", invokeName, invokeLength, system, group);

        return jdbi.withHandle(handle ->
                infoComponent.getMethodMock(handle, checkGroup(group), invokeName, invokeLength))
                .map(data -> Mono.just(ResultWrapper.success(jsons.mapJson(data))))
                .orElseGet(() -> method_detail(group, system, invokeName, invokeLength)
                        .flatMap(one -> Mono.just(ResultWrapper.success(
                                jsons.mapJson(String.valueOf(one.getOrDefault("returnMock", "{}")))))));
    }


    private String checkGroup(String group) {
        if (null == group) {
            group = PlatformConstants.DEFAULT_GROUP;
        }
        return group;
    }

    /**
     * 加入导入信息
     */
    private void checkClazzName(Map<String, Optional<InfoClazzField>> refClazzField, Set<String> refClazzNames,
                                List<FieldDetailInfo> parameterInfo, FieldDetailInfo returnInfo) {
        //导入的类信息
        final Set<String> importClazzNames = refClazzField.values().stream().map(clazzOne -> {
            final byte[] importInfo = clazzOne.orElse(new InfoClazzField()).getImportInfo();
            Set<String> imports = Collections.emptySet();
            if (null != importInfo) {
                imports = decodeColumn(importInfo);
            }
            return imports;
        }).flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!importClazzNames.isEmpty()) {
            refClazzNames.addAll(importClazzNames);
        }
        //导入的父类信息
        final Set<String> superClazzNames = refClazzField.values().stream().map(clazzOne -> {
            final byte[] superclassInfo = clazzOne.orElse(new InfoClazzField()).getSuperclassInfo();
            Set<String> supers = Collections.emptySet();
            if (null != superclassInfo) {
                supers = decodeColumn(superclassInfo);
            }
            return supers;
        }).flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!superClazzNames.isEmpty()) {
            refClazzNames.addAll(superClazzNames);
        }
        //导入的范型信息
        if (null != parameterInfo && !parameterInfo.isEmpty()) {
            parameterInfo.forEach(paramOne -> {
                if (null != paramOne.getGenericTypeName()) {
                    refClazzNames.addAll(paramOne.getGenericTypeName().stream()
                            .map(GenericSimpleInfo::getTypeName).collect(Collectors.toSet()));
                }
            });
        }
        if (null != returnInfo && null != returnInfo.getGenericTypeName()) {
            refClazzNames.addAll(returnInfo.getGenericTypeName().stream()
                    .map(GenericSimpleInfo::getTypeName).collect(Collectors.toSet()));
        }

        log.info("[GATEWAY] 当前导入要查找的类型信息: {}", refClazzNames);
    }

    /**
     * 合并分组，查询会查出stable和输入分组
     */
    private Map<String, Optional<InfoClazzField>> getField(String group, Set<String> clazzNames) {
        return jdbi.withHandle(handle ->
                infoComponent.findClazzField(handle, checkGroup(group), Lists.newArrayList(clazzNames)).stream())
                .collect(Collectors.groupingBy(InfoClazzField::getFullName, Collectors.reducing((left, right) -> {
                    if (PlatformConstants.GROUP.equalsIgnoreCase(left.getGroupName())) {
                        return left;
                    }
                    if (PlatformConstants.GROUP.equalsIgnoreCase(right.getGroupName())) {
                        return right;
                    }
                    if (PlatformConstants.DEFAULT_GROUP.equalsIgnoreCase(left.getGroupName())) {
                        return left;
                    }
                    if (PlatformConstants.DEFAULT_GROUP.equalsIgnoreCase(right.getGroupName())) {
                        return right;
                    }
                    if (left.getUpdatedAt().after(right.getUpdatedAt())) {
                        return left;
                    } else {
                        return right;
                    }
                })));
    }


    /**
     * 设置子类类型
     */
    private void clazzType(FieldDetailInfo fieldDetailInfo, Map<String, Optional<InfoClazzField>> classFieldMap) {
        if (null == fieldDetailInfo || null == fieldDetailInfo.getFieldName() || null == fieldDetailInfo.getSimpleTypeName()) {
            return;
        }
        //内部类型不需要继续调用
        if (fieldDetailInfo.getInnerType()) {
            return;
        }
        if (fieldDetailInfo.getGenericTypeDepth() <= 0 && InnerTypeUtil.isJavaType(fieldDetailInfo.getFullTypeName())) {
            fieldDetailInfo.setInnerType(true);
            return;
        }

        //设置字段类型
        final String typeName = fieldDetailInfo.getFullTypeName();
        if (null != typeName) {
            recurField(fieldDetailInfo, classFieldMap, typeName);
        }
    }

    /**
     * 递归设置子类类型
     */
    private void recurField(FieldDetailInfo fieldDetailInfo, Map<String, Optional<InfoClazzField>> classFieldMap, String typeName) {
        final Optional<InfoClazzField> optionInfoClazzField = classFieldMap.getOrDefault(typeName, Optional.empty());
        if (optionInfoClazzField.isPresent()) {
            final InfoClazzField oneClazzField = optionInfoClazzField.get();
            if (null != oneClazzField.getCommentInfo()) {
                final CommentInfo commentInfo = decodeColumn(oneClazzField.getCommentInfo());
                setComment(fieldDetailInfo, commentInfo);
            }

            //合并父类字段
            mergeSuperField(fieldDetailInfo, classFieldMap, oneClazzField);
            //替换泛型信息
            replaceFieldGeneric(fieldDetailInfo, classFieldMap, oneClazzField);

        } else {
            //如果为内部容器，检测泛型并只展示第一个不是内部类型的类型，最多处理两级
            if (fieldDetailInfo.getGenericTypeDepth() > 0 && null != fieldDetailInfo.getGenericTypeName()) {
                for (GenericSimpleInfo genericType : fieldDetailInfo.getGenericTypeName()) {
                    if (genericClazzField(fieldDetailInfo, classFieldMap, genericType)) {
                        break;
                    }

                    final List<GenericSimpleInfo> genericSubType = genericType.getSubTypeName();
                    if (CollectionUtil.notEmpty(genericSubType)) {
                        for (GenericSimpleInfo genericSubTypeInfo : genericSubType) {
                            if (genericClazzField(fieldDetailInfo, classFieldMap, genericSubTypeInfo)) {
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * 更新或者设置字段
     */
    private List<FieldDetailInfo> upsertFieldList(FieldDetailInfo fieldDetailInfo, InfoClazzField subClazzField) {
        List<FieldDetailInfo> subClazzInfo = decodeColumn(subClazzField.getFieldInfo());
        List<FieldDetailInfo> fieldDetailList = fieldDetailInfo.getFieldList();
        if (null == fieldDetailList) {
            fieldDetailList = subClazzInfo;
        } else {
            fieldDetailList.addAll(subClazzInfo);
        }
        //防止父类重复字段，并过滤掉注入参数
        fieldDetailInfo.setFieldList(fieldDetailList.stream().distinct()
                .filter(one -> !one.getEnquire()).collect(Collectors.toList()));

        //注意只返回解析的字段，不包括已有的
        return subClazzInfo;
    }

    /**
     * 合并父类字段
     */
    private void mergeSuperField(FieldDetailInfo fieldDetailInfo, Map<String, Optional<InfoClazzField>> classFieldMap,
                                 InfoClazzField oneClazzField) {
        List<FieldDetailInfo> superClazzInfo = null;
        if (null != oneClazzField.getSuperclassInfo()) {
            final Set<String> superclassInfo = decodeColumn(oneClazzField.getSuperclassInfo());
            for (String superclass : superclassInfo) {
                Optional<InfoClazzField> optionSuperClazzField = classFieldMap.getOrDefault(superclass, Optional.empty());
                if (optionSuperClazzField.isPresent()) {
                    final InfoClazzField superClazzField = optionSuperClazzField.get();
                    if (null != superClazzField.getFieldInfo()) {
                        superClazzInfo = upsertFieldList(fieldDetailInfo, superClazzField);
                    }
                }
            }
        }
        if (CollectionUtil.notEmpty(superClazzInfo)) {
            superClazzInfo.forEach(one -> clazzType(one, classFieldMap));
        }
    }

    /**
     * 替换泛型信息
     */
    private void replaceFieldGeneric(FieldDetailInfo fieldDetailInfo, Map<String, Optional<InfoClazzField>> classFieldMap,
                                     InfoClazzField oneClazzField) {
        List<FieldDetailInfo> nowClazzInfo = null;
        if (oneClazzField.getIsGeneric() && null != oneClazzField.getGenericInfo()) {
            final List<GenericSimpleInfo> genericType = fieldDetailInfo.getGenericTypeName();
            final List<String> genericList = decodeColumn(oneClazzField.getGenericInfo());

            //如果是容器设置容器类型
            if (CollectionUtil.notEmpty(genericType)) {
                nowClazzInfo = upsertFieldList(fieldDetailInfo, oneClazzField);
                //泛型列表与参数泛型类型长度相等
                final int size = nowClazzInfo.size();
                for (int i = 0; i < size; i++) {
                    final FieldDetailInfo oneDetailInfo = nowClazzInfo.get(i);
                    //存储的类定义范型值与字段类型名做比较
                    final int index = genericList.indexOf(oneDetailInfo.getSimpleTypeName());
                    if (index >= 0) {
                        final GenericSimpleInfo oneGenericType = genericType.get(index);
                        final Optional<InfoClazzField> optionClazzInfo = classFieldMap.getOrDefault
                                (oneGenericType.getTypeName(), Optional.empty());
                        if (optionClazzInfo.isPresent()) {
                            final InfoClazzField oneInfoClazzField = optionClazzInfo.get();
                            oneDetailInfo.setSimpleTypeName(oneInfoClazzField.getSimpleName());
                            oneDetailInfo.setFullTypeName(oneInfoClazzField.getFullName());
                            setComment(oneDetailInfo, decodeColumn(oneInfoClazzField.getCommentInfo()));
                            if (null != oneGenericType.getSubTypeName()) {
                                oneDetailInfo.setGenericTypeName(oneGenericType.getSubTypeName());
                            }
                        } else {
                            oneDetailInfo.setSimpleTypeName(oneGenericType.getSimpleName());
                            oneDetailInfo.setFullTypeName(oneGenericType.getTypeName());
                            oneDetailInfo.setInnerType(true);
                        }
                        nowClazzInfo.set(i, oneDetailInfo);
                    } else {
                        //如果当前字段是一个容器，解析内部类型，最多解析两层
                        final List<GenericSimpleInfo> genericTypeName = oneDetailInfo.getGenericTypeName();
                        if (oneDetailInfo.getGenericTypeDepth() > 0 && null != genericTypeName) {
                            boolean innerType = true;
                            for (GenericSimpleInfo genericSimpleType : genericTypeName) {
                                //存储的类定义范型值与字段类型名做比较
                                if (!genericClazzField(genericList, genericType, genericSimpleType)) {
                                    final List<GenericSimpleInfo> genericSubType = genericSimpleType.getSubTypeName();
                                    if (CollectionUtil.notEmpty(genericSubType)) {
                                        for (GenericSimpleInfo genericSimpleSubType : genericSubType) {
                                            genericClazzField(genericList, genericSubType, genericSimpleSubType);
                                        }
                                    }
                                }
                                if (!InnerTypeUtil.isInnerType(genericSimpleType.getTypeName())) {
                                    innerType = false;
                                }
                            }
                            oneDetailInfo.setInnerType(innerType);

                            if (innerType && StringUtil.isBlank(oneDetailInfo.getExtendComment())) {
                                final int genericSize = genericTypeName.size();
                                StringBuilder comment = new StringBuilder("内部类型是: ");
                                for (int j = 0; j < genericSize; j++) {
                                    GenericSimpleInfo typeInfo = genericTypeName.get(i);
                                    comment.append(typeInfo.getSimpleName());
                                    if (j < genericSize - 1) {
                                        comment.append(", ");
                                    }
                                }
                                oneDetailInfo.setExtendComment(comment.toString());
                            }
                        } else {
                            oneDetailInfo.setInnerType(true);
                        }
                    }
                }
            }
        }

        if (null == nowClazzInfo && null != oneClazzField.getFieldInfo()) {
            nowClazzInfo = upsertFieldList(fieldDetailInfo, oneClazzField);
        }
        if (CollectionUtil.notEmpty(nowClazzInfo)) {
            nowClazzInfo.forEach(one -> clazzType(one, classFieldMap));
        }
    }

    /**
     * 容器内部类型处理
     */
    private boolean genericClazzField(FieldDetailInfo fieldDetailInfo, Map<String, Optional<InfoClazzField>> classFieldMap, GenericSimpleInfo genericType) {
        final Optional<InfoClazzField> optionGenericClazzField = classFieldMap.getOrDefault(genericType.getTypeName(), Optional.empty());
        if (optionGenericClazzField.isPresent()) {
            final InfoClazzField genericClazzField = optionGenericClazzField.get();

            //合并父类信息
            mergeSuperField(fieldDetailInfo, classFieldMap, genericClazzField);
            //处理本身字段
            if (null != genericClazzField.getFieldInfo()) {
                List<FieldDetailInfo> genericFieldList = upsertFieldList(fieldDetailInfo, genericClazzField);
                genericFieldList.forEach(one -> clazzType(one, classFieldMap));
                return true;
            }
        }
        return false;
    }

    /**
     * 容器内部类型处理
     */
    private boolean genericClazzField(List<String> genericList, List<GenericSimpleInfo> genericType, GenericSimpleInfo nowGenericType) {
        final int index = genericList.indexOf(nowGenericType.getSimpleName());
        if (index >= 0) {
            final GenericSimpleInfo oneGenericType = genericType.get(index);
            nowGenericType.setSimpleName(oneGenericType.getSimpleName());
            nowGenericType.setTypeName(oneGenericType.getTypeName());
            nowGenericType.setLastTypeName(oneGenericType.getLastTypeName());
            return true;
        }
        return false;
    }

    /**
     * 设置注释
     */
    private void setComment(FieldDetailInfo fieldDetailInfo, CommentInfo commentInfo) {
        if (null != fieldDetailInfo && null != commentInfo) {
            if (StringUtil.isBlank(fieldDetailInfo.getSimpleComment())) {
                fieldDetailInfo.setSimpleComment(commentInfo.getTitle());
                fieldDetailInfo.setExtendComment(commentInfo.getDetail());
            } else {
                fieldDetailInfo.setExtendComment(commentInfo.getDetail());
            }
        }
    }

    private <T> T decodeColumn(byte[] input) {
        return KryoBaseUtil.readFromByteArray(input);
    }
}
