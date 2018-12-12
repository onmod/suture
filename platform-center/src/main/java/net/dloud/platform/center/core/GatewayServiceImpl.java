package net.dloud.platform.center.core;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.crypto.tink.subtle.Base64;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.client.GatewayService;
import net.dloud.platform.common.domain.entry.GroupEntry;
import net.dloud.platform.common.domain.result.GatewayGroupResult;
import net.dloud.platform.common.domain.result.GatewayMethodResult;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.InjectEnum;
import net.dloud.platform.common.gateway.bean.InvokeKey;
import net.dloud.platform.common.gateway.info.ClassInfo;
import net.dloud.platform.common.gateway.info.CommentInfo;
import net.dloud.platform.common.gateway.info.FieldDetailInfo;
import net.dloud.platform.common.gateway.info.FieldInfo;
import net.dloud.platform.common.gateway.info.GenericSimpleInfo;
import net.dloud.platform.common.gateway.info.InjectionInfo;
import net.dloud.platform.common.gateway.info.MethodInfo;
import net.dloud.platform.common.gateway.info.ParameterInfo;
import net.dloud.platform.common.gateway.info.PermissionInfo;
import net.dloud.platform.common.gateway.info.TypeInfo;
import net.dloud.platform.common.serialize.InnerTypeUtil;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.dal.InfoComponent;
import net.dloud.platform.dal.entity.InfoClazzEntity;
import net.dloud.platform.dal.entity.InfoClazzVersion;
import net.dloud.platform.dal.entity.InfoGroupEntity;
import net.dloud.platform.dal.entity.InfoMethodEntity;
import net.dloud.platform.dal.entity.InfoMethodVersion;
import net.dloud.platform.extend.constant.CenterEnum;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.tuple.PairTuple;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import net.dloud.platform.parse.module.AssistComponent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author QuDasheng
 * @create 2018-09-12 16:55
 **/
@Slf4j
@Service
public class GatewayServiceImpl implements GatewayService {
    /**
     * 默认服务前缀
     */
    private final String servicePrefix = "_";
    /**
     * 默认服务后缀
     */
    private final String serviceSuffix = "Service";
    /**
     * 默认分隔符
     */
    private final String serviceSpilit = "\\.";


    @Value("#{'${clazz.import-suffix}'.split(',')}")
    private Set<String> importSuffix;

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private InfoComponent infoComponent;

    @Autowired
    private AssistComponent assistComponent;


    @Override
    public GatewayGroupResult groupInfo(GroupEntry groupInfo) {
        final String groupName = groupInfo.getGroupName();
        AssertWrapper.notBlank(groupName, "分组信息不能为空");
        final String currentIp = groupInfo.getCurrentIp();
        AssertWrapper.notBlank(currentIp, "主机信息不能为空");
        final String versionInfo = groupInfo.getVersionInfo();
        AssertWrapper.notBlank(versionInfo, "版本信息不能为空");

        final Timestamp now = Timestamp.from(Instant.now());
        final GatewayGroupResult result = new GatewayGroupResult();

//        InterProcessLock lock = new InterProcessMutex(client, lockPath);
        final InfoGroupEntity info = jdbi.withHandle(handle ->
                infoComponent.getGroup(handle, groupName, groupInfo.getSystemId()).orElse(new InfoGroupEntity()));
        log.info("[GATEWAY] 输入的组信息: {}, 查询到的组信息: {}", groupInfo, info);

        if (null == info.getGroupName() || null == info.getSystemId()) {
            result.setNewgroup(true);
            info.setGroupName(groupName);
            info.setSystemId(groupInfo.getSystemId());
            info.setSystemName(groupInfo.getSystemName());
            info.setVersionInfo(versionInfo);
            info.setCurrentIp(convertField(Sets.newHashSet(currentIp)));
            info.setCreatedAt(now);
            info.setUpdatedAt(now);
            jdbi.withHandle(handle -> infoComponent.insertGroup(handle, info));
        } else {
            //版本是否一致
            if (versionInfo.equalsIgnoreCase(info.getVersionInfo())) {
                result.setConsistent(true);
            } else {
                info.setVersionInfo(versionInfo);
            }
            final Set<String> ips = readField(info.getCurrentIp());
            if (!result.isConsistent() || !ips.contains(currentIp)) {
                ips.add(currentIp);
                log.info("[GATEWAY] 新的ip组数据: {}", ips);
                info.setCurrentIp(convertField(ips));
                info.setUpdatedAt(now);
                jdbi.withHandle(handle -> infoComponent.updateGroup(handle, info));
            }
        }

        log.info("[GATEWAY] 返回的组信息结果: {}", result);
        return result;
    }

    @Override
    public GatewayMethodResult clazzInfo(GroupEntry groupInfo, boolean newGroup, byte[] clazzInfo) {
        final Map<String, String> indexInfos = KryoBaseUtil.readFromByteArray(clazzInfo, true, false);

        final GatewayMethodResult result = new GatewayMethodResult();
        if (newGroup) {
            if (PlatformConstants.DEFAULT_GROUP.equalsIgnoreCase(groupInfo.getGroupName())) {
                result.setClassList(Lists.newArrayList(indexInfos.keySet()));
                result.setClassVersion(indexInfos);
            } else {
                final Map<String, String> versionClazz = jdbi.withHandle(handle ->
                        infoComponent.findVersionClazz(handle, PlatformConstants.DEFAULT_GROUP, groupInfo.getSystemId()))
                        .stream().collect(Collectors.toMap(InfoClazzVersion::getFullName, InfoClazzVersion::getVersionInfo));
                insertSelectClazz(result, groupInfo, true, indexInfos, versionClazz);
            }
        } else {
            final Map<String, String> versionClazz = jdbi.withHandle(handle ->
                    infoComponent.findVersionClazz(handle, groupInfo.getGroupName(), groupInfo.getSystemId()))
                    .stream().collect(Collectors.toMap(InfoClazzVersion::getFullName, InfoClazzVersion::getVersionInfo));
            insertSelectClazz(result, groupInfo, false, indexInfos, versionClazz);
        }

        log.info("[GATEWAY] 当前需要合并的类信息: {}", result);
        return result;
    }

    @Override
    public GatewayMethodResult methodInfo(GroupEntry groupInfo, Map<String, String> clazzVersion, byte[][] methodInfo) {
        final int length = methodInfo.length;
        AssertWrapper.isTrue(clazzVersion.size() == length, "输入的信息不匹配");

        final Timestamp now = Timestamp.from(Instant.now());
        final GatewayMethodResult result = new GatewayMethodResult();

        int i = 0;
        List<String> className = Lists.newArrayListWithExpectedSize(length);
        List<ClassInfo> classList = Lists.newArrayListWithExpectedSize(length);
        try {
            for (i = 0; i < length; i++) {

                final ClassInfo classInfo = KryoBaseUtil.readFromByteArray(methodInfo[i], true, false);
                className.add(classInfo.getQualifiedName());
                classList.add(classInfo);
            }
        } catch (Exception ex) {
            log.warn("[GATEWAY] 第[{}]次解析发生异常, 输入信息: {}", i, Base64.encode(methodInfo[i]));
            throw ex;
        }

        // 查询数据库中已存在的信息
        final List<InfoMethodVersion> versionMethodList = jdbi.withHandle(handle ->
                infoComponent.findVersionMethod(handle, groupInfo.getGroupName(), className));
        final Set<String> versionMethod = versionMethodList.stream().map(e -> e.getInvokeName() + '|' + e.getInvokeLength())
                .collect(Collectors.toSet());
        final Set<String> versionClazz = versionMethodList.stream().map(InfoMethodVersion::getClazzName).collect(Collectors.toSet());
        log.info("[GATEWAY] 当前查找到类信息: {}, 方法信息: {}", versionClazz, versionMethod);

        if (!clazzVersion.isEmpty()) {
            mergeClazzAndMethod(result, groupInfo, clazzVersion, classList, versionClazz, versionMethod, now);
        }

        return result;
    }

    private void insertSelectClazz(GatewayMethodResult result, GroupEntry groupInfo, boolean newGroup,
                                   Map<String, String> indexInfos, Map<String, String> versionClazz) {
        final String groupName = groupInfo.getGroupName();
        final int systemId = groupInfo.getSystemId();

        final Map<String, String> newVersion = Maps.newLinkedHashMap();
        //新增更新
        List<String> clazzList = Lists.newArrayList();
        //已存在
        List<String> clazzExist = Lists.newArrayList();
        for (String name : indexInfos.keySet()) {
            final String inputVersion = indexInfos.get(name);
            final String selectVersion = versionClazz.get(name);
            if (null == selectVersion) {
                clazzList.add(name);
                newVersion.put(name, inputVersion);
            } else {
                //删除获取过的类
                versionClazz.remove(name);
                if (selectVersion.equalsIgnoreCase(inputVersion)) {
                    clazzExist.add(name);
                } else {
                    clazzList.add(name);
                    newVersion.put(name, inputVersion);
                }
            }
        }

        jdbi.useTransaction(handle -> {
            //新组并且信息和基组一样的选择插入
            if (newGroup && !clazzExist.isEmpty()) {
                log.info("[GATEWAY] 要插入的类信息", clazzExist);
                infoComponent.insertSelectClazz(handle, PlatformConstants.DEFAULT_GROUP, groupName,
                        systemId, clazzExist);
                infoComponent.insertSelectMethod(handle, PlatformConstants.DEFAULT_GROUP, groupName,
                        systemId, clazzExist);
            }
            //非新组并且查询到版本信息有多余的应该删除
            if (!newGroup && !versionClazz.isEmpty()) {
                final List<String> clazzDelete = Lists.newArrayList(versionClazz.keySet());
                log.info("[GATEWAY] 要删除的类信息", clazzDelete);
                infoComponent.batchDeleteClazz(handle, groupName, clazzDelete);
                infoComponent.batchDeleteMethodByClass(handle, groupName, clazzDelete);
            }
            result.setClassList(clazzList);
            result.setClassVersion(newVersion);
        });
    }


    private void mergeClazzAndMethod(GatewayMethodResult result, GroupEntry groupInfo, Map<String, String> clazzVersion, List<ClassInfo> classList,
                                     Set<String> existClazz, Set<String> existMethod, Timestamp now) {
        final List<InfoClazzEntity> clazzList = Lists.newArrayListWithExpectedSize(classList.size());
        final List<InfoMethodEntity> methodList = Lists.newArrayList();

        final String groupName = groupInfo.getGroupName();
        final int systemId = groupInfo.getSystemId();

        for (ClassInfo classInfo : classList) {
            final InfoClazzEntity infoClazz = new InfoClazzEntity();
            final String fullName = classInfo.getQualifiedName();
            final String simpleName = classInfo.getSimpleName();
            infoClazz.setGroupName(groupName);
            infoClazz.setSystemId(systemId);
            infoClazz.setFullName(fullName);
            infoClazz.setSimpleName(simpleName);
            //父类信息
            final Set<TypeInfo> superclass = classInfo.getSuperclass();
            if (null != superclass) {
                final Set<String> superclassNames = Sets.newHashSetWithExpectedSize(superclass.size());
                for (TypeInfo typeInfo : superclass) {
                    superclassNames.add(typeInfo.getQualifiedName());
                }
                infoClazz.setSuperclassInfo(convertField(superclassNames));
            }
            //泛型信息
            final List<String> genericList = classInfo.getGenericList();
            if (null != genericList) {
                infoClazz.setGenericInfo(convertField(genericList));
            }
            //导入信息
            final Set<String> importInfo = classInfo.getImportInfo();
            if (null != importInfo && !importInfo.isEmpty()) {
                infoClazz.setImportInfo(convertField(importInfo));
            }
            //版本信息
            infoClazz.setVersionInfo(clazzVersion.get(fullName));
            setField(classInfo, infoClazz);
            final CommentInfo classComment = classInfo.getCommentInfo();
            //注释和简单注释
            if (null != classComment) {
                infoClazz.setCommentInfo(convertField(classComment));
                infoClazz.setSimpleComment(simpleComment(classComment));
            }

            //是否会展示到文档
            if (classInfo.getIfInterface() && importSuffix.contains(StringUtil.classSuffix(simpleName))) {
                infoClazz.setIsInterface(true);
            } else {
                infoClazz.setIsInterface(false);
            }
            infoClazz.setIsGeneric(classInfo.getIfGeneric());
            infoClazz.setIsPrimitive(classInfo.getIfPrimitive());

            if (existClazz.contains(fullName)) {
                infoClazz.setCreatedAt(now);
            }
            infoClazz.setUpdatedAt(now);
            clazzList.add(infoClazz);

            if (infoClazz.getIsInterface() && null != classInfo.getMethodInfo()) {
                for (MethodInfo methodInfo : classInfo.getMethodInfo()) {
                    final InfoMethodEntity infoMethod = new InfoMethodEntity();
                    final String invokeName = methodInfo.getInvokeName();

                    infoMethod.setGroupName(groupName);
                    infoMethod.setSystemId(systemId);
                    infoMethod.setClazzName(fullName);
                    infoMethod.setPathName(pathName(invokeName));
                    infoMethod.setInvokeName(invokeName);
                    infoMethod.setSimpleName(methodInfo.getSimpleName());
                    //设置参数
                    setParameter(methodInfo, infoMethod);
                    setReturn(methodInfo, infoMethod);
                    //设置注释
                    final CommentInfo commentInfo = methodInfo.getCommentInfo();
                    if (null != commentInfo) {
                        infoMethod.setCommentInfo(convertField(commentInfo));
                        infoMethod.setSimpleComment(simpleComment(commentInfo));
                    }

                    final PermissionInfo permissionInfo = methodInfo.getPermissionInfo();
                    if (null != permissionInfo) {
                        infoMethod.setPermissionInfo(convertField(permissionInfo));
                    }
                    infoMethod.setIsWhitelist(null == methodInfo.getIfWhitelist() ? false : methodInfo.getIfWhitelist());
                    infoMethod.setIsBackground(null == methodInfo.getIfBackground() ? false : methodInfo.getIfBackground());
                    infoMethod.setCreatedAt(now);
                    infoMethod.setUpdatedAt(now);
                    methodList.add(infoMethod);
                }
            }
        }

        jdbi.useTransaction(handle -> {
            // 先删除后存储
            if (!existMethod.isEmpty()) {
                log.info("[GATEWAY] 要删除的方法信息: {}, 方法条数: {}", existMethod, existMethod.size());
                existMethod.forEach(invokeKey -> {
                    final String[] split = invokeKey.split("\\|");
                    if (split.length == 2) {
                        infoComponent.deleteMethod(handle, groupName, split[0], Integer.valueOf(split[1]));
                    }
                });
            }

            if (!clazzList.isEmpty()) {
                final int mergeClazz = infoComponent.mergeClazz(handle, clazzList);
                log.info("[GATEWAY] 保存类成功条数: {}, 输入条数: {}", mergeClazz, classList.size());
            }
            if (!methodList.isEmpty()) {
                final int mergeMethod = infoComponent.mergeMethod(handle, methodList);
                log.info("[GATEWAY] 保存方法成功条数: {}, 输入条数: {}", mergeMethod, methodList.size());
            }
        });

        //删除网关中的缓存
        try {
            if (!methodList.isEmpty()) {
                final List<InvokeKey> invokeList = Lists.newArrayListWithExpectedSize(methodList.size());
                for (InfoMethodEntity method : methodList) {
                    final InvokeKey invokeKey = new InvokeKey(method.getGroupName(), method.getInvokeName(), method.getInvokeLength());
                    assistComponent.publish(CenterEnum.GATEWAY_CENTER, "invokeKey", invokeKey);
                    invokeList.add(invokeKey);
                }
                log.info("[GATEWAY] 要删除的网关中的缓存: {}", invokeList);
            }
        } catch (Exception e) {
            log.warn("[GATEWAY] 删除网关中的缓存失败: ", e);
        }
    }

    private String pathName(String invokeName) {
        if (null == invokeName) {
            throw new PassedException("网关加载文件生成错误");
        }
        final String[] invokes = invokeName.split(serviceSpilit);
        if (invokes.length != 3) {
            throw new PassedException("网关加载文件生成错误");
        }

        String system = StringUtil.firstLowerCase(invokes[0]);
        String clazz = StringUtil.firstLowerCase(invokes[1]);
        String method = StringUtil.firstLowerCase(invokes[2]);
        final StringBuilder builder = new StringBuilder("/").append(system).append("/");
        final int suffixIndex = clazz.lastIndexOf(serviceSuffix);
        if (suffixIndex >= 0) {
            clazz = clazz.substring(0, suffixIndex);
        }
        if (system.equals(clazz)) {
            builder.append(servicePrefix).append(method);
        } else {
            builder.append(clazz).append("/").append(method);
        }
        return builder.toString();
    }

    private String simpleComment(CommentInfo commentInfo) {
        if (null == commentInfo) {
            return null;
        }
        final String title = commentInfo.getTitle();
        if (null == title) {
            return commentInfo.getDetail();
        } else {
            return commentInfo.getTitle();
        }
    }

    private void setField(ClassInfo classInfo, InfoClazzEntity infoClazz) {
        List<FieldInfo> fieldInfos = classInfo.getFieldInfo();
        if (null != fieldInfos) {
            List<FieldDetailInfo> fieldDetail = Lists.newArrayListWithExpectedSize(fieldInfos.size());
            for (FieldInfo fieldInfo : fieldInfos) {
                fieldDetail.add(selectType(fieldInfo.getFieldName(), fieldInfo.getTypeInfo(),
                        fieldInfo.getCommentInfo(), fieldInfo.getIfEnquire(), false));
            }

            infoClazz.setFieldInfo(convertField(fieldDetail));
        }
    }

    private void setParameter(MethodInfo methodInfo, InfoMethodEntity infoMethod) {
        int invokeLength = 0;

        List<FieldDetailInfo> fieldDetails = Lists.newArrayList();
        Map<String, ParameterInfo> parameterInfos = methodInfo.getParameterInfo();
        if (null != parameterInfos) {
            invokeLength = parameterInfos.size();
            final Map<String, String> simpleParameter = Maps.newLinkedHashMapWithExpectedSize(invokeLength);
            final Map<String, InjectionInfo> injectionMap = Maps.newLinkedHashMapWithExpectedSize(invokeLength);
            //遍历所有参数
            for (Map.Entry<String, ParameterInfo> keyvalInfo : parameterInfos.entrySet()) {
                String parameterName = keyvalInfo.getKey();
                ParameterInfo parameterInfo = keyvalInfo.getValue();
                final TypeInfo paramType = parameterInfo.getParamType();
                final String paramTypeName = paramType.getQualifiedName();

                if (parameterInfo.getIfInjection()) {
                    final InjectionInfo injectionInfo = parameterInfo.getInjectionInfo();
                    injectionMap.put(parameterName, injectionInfo);
                    if (InjectEnum.getLevel(injectionInfo.getInjectType()) > 1
                            || InnerTypeUtil.isInnerTypeByKey(paramType.getSimpleName())) {
                        invokeLength--;
                    }
                }
                fieldDetails.add(selectType(parameterName, paramType, methodInfo.getCommentInfo(), false, true));
                //设置简单参数列表
                simpleParameter.put(parameterName, paramTypeName);
            }

            infoMethod.setSimpleParameter(convertField(simpleParameter));
            infoMethod.setParameterInfo(convertField(fieldDetails));
            if (!injectionMap.isEmpty()) {
                infoMethod.setInjectionInfo(convertField(injectionMap));
            }
        }

        infoMethod.setInvokeLength(invokeLength);
    }

    private void setReturn(MethodInfo methodInfo, InfoMethodEntity infoMethod) {
        infoMethod.setReturnInfo(convertField(selectType("", methodInfo.getReturnInfo(),
                methodInfo.getCommentInfo(), false, true)));
    }

    /**
     * 设置类型
     */
    private FieldDetailInfo selectType(String name, TypeInfo typeInfo, CommentInfo commentInfo,
                                       boolean ifEnquire, boolean ifParameter) {
        final FieldDetailInfo clazzField = new FieldDetailInfo();
        final String simpleName = typeInfo.getSimpleName();
        final String qualifiedName = typeInfo.getQualifiedName();

        clazzField.setFieldName(name);
        clazzField.setSimpleTypeName(simpleName);
        clazzField.setFullTypeName(qualifiedName);
        if (null != commentInfo) {
            if (ifParameter) {
                if (name.isEmpty()) {
                    clazzField.setSimpleComment(commentInfo.getReturned());
                } else {
                    if (null != commentInfo.getParams()) {
                        clazzField.setSimpleComment(commentInfo.getParams().get(name));
                    }
                }
            } else {
                final String title = commentInfo.getTitle();
                if (null != title) {
                    clazzField.setSimpleComment(title);
                    clazzField.setExtendComment(commentInfo.getDetail());
                } else {
                    clazzField.setSimpleComment(commentInfo.getDetail());
                }
            }
        }

        clazzField.setEnquire(ifEnquire);
        if (typeInfo.getIfPrimitive() || InnerTypeUtil.isJavaType(qualifiedName)) {
            clazzField.setInnerType(true);
            if (typeInfo.getIfArray()) {
                final String primitiveName = InnerTypeUtil.primitiveName(StringUtil.splitLastByDot(
                        qualifiedName).replaceAll("\\[]", ""));
                clazzField.setGenericTypeName(Lists.newArrayList(new GenericSimpleInfo(
                        simpleName, primitiveName, null, null)));
                clazzField.setGenericTypeDepth(1);
                clazzField.setExtendComment("内部类型是 " + primitiveName);
            }
            if (typeInfo.getIfGeneric()) {
                final List<TypeInfo> genericInfo = typeInfo.getGenericInfo();
                setGeneric(genericInfo, clazzField, true);
            }
        } else if (typeInfo.getIfArray()) {
            clazzField.setGenericTypeName(Lists.newArrayList(new GenericSimpleInfo(
                    simpleName, qualifiedName.replaceAll("\\[]", ""), null, null)));
            clazzField.setGenericTypeDepth(1);
        } else if (typeInfo.getIfGeneric()) {
            setGeneric(typeInfo.getGenericInfo(), clazzField, false);
        }

        return clazzField;
    }

    /**
     * 设置范型信息
     * 这里的 isJavaType 是上一步已经判定过的值
     */
    private void setGeneric(List<TypeInfo> genericInfo, FieldDetailInfo detailInfo, boolean isJavaType) {
        if (null == genericInfo || genericInfo.isEmpty()) {
            return;
        }

        int size = genericInfo.size();
        boolean innerType = true;
        List<GenericSimpleInfo> genericNames = Lists.newArrayListWithExpectedSize(genericInfo.size());
        for (int i = 0; i < size; i++) {
            final TypeInfo typeInfo = genericInfo.get(i);
            final String qualifiedName = typeInfo.getQualifiedName();
            //如果是内部类型，展示简名，不需要查找
            if (!isJavaType || !InnerTypeUtil.isJavaType(qualifiedName)) {
                innerType = false;
            }
            final GenericSimpleInfo genericSimpleInfo = new GenericSimpleInfo(typeInfo.getSimpleName(), qualifiedName, null, null);
            genericNames.add(genericSimpleInfo);
            if (typeInfo.getIfGeneric()) {
                PairTuple<Boolean, String> result = setGeneric(typeInfo.getGenericInfo(), detailInfo, genericSimpleInfo);
                innerType = result.getFirst();
                genericSimpleInfo.setLastTypeName(result.getLast());
            }
        }

        detailInfo.setInnerType(innerType);
        detailInfo.setGenericTypeName(genericNames);
        detailInfo.setGenericTypeDepth(detailInfo.getGenericTypeDepth() + 1);
        if (innerType && StringUtil.isBlank(detailInfo.getExtendComment())) {
            StringBuilder comment = new StringBuilder("内部类型是: ");
            for (int i = 0; i < size; i++) {
                TypeInfo typeInfo = genericInfo.get(i);
                comment.append(typeInfo.getSimpleName());
                if (i < size - 1) {
                    comment.append(", ");
                }
            }
            detailInfo.setExtendComment(comment.toString());
        }
    }

    /**
     * 展开全部泛型信息，之后在imports中查找，找到则替换
     */
    private PairTuple<Boolean, String> setGeneric(List<TypeInfo> genericInfo, FieldDetailInfo detailInfo, GenericSimpleInfo genericSimpleInfo) {
        detailInfo.setGenericTypeDepth(detailInfo.getGenericTypeDepth() + 1);

        boolean innerType = true;
        String lastTypeName = null;
        List<GenericSimpleInfo> subTypeNames = genericSimpleInfo.getSubTypeName();
        if (null == subTypeNames) {
            subTypeNames = Lists.newArrayList();
        }
        for (TypeInfo typeInfo : genericInfo) {
            final String qualifiedName = typeInfo.getQualifiedName();
            if (!typeInfo.getIfPrimitive() && !InnerTypeUtil.isJavaType(qualifiedName)) {
                innerType = false;
                lastTypeName = typeInfo.getQualifiedName();
            }

            final GenericSimpleInfo subGenericSimpleInfo = new GenericSimpleInfo(typeInfo.getSimpleName(), qualifiedName, null, null);
            subTypeNames.add(subGenericSimpleInfo);
            if (typeInfo.getIfGeneric()) {
                PairTuple<Boolean, String> result = setGeneric(typeInfo.getGenericInfo(), detailInfo, subGenericSimpleInfo);
                innerType = result.getFirst();
                lastTypeName = result.getLast();
            }
        }
        if (null == lastTypeName && !genericInfo.isEmpty()) {
            lastTypeName = genericInfo.get(genericInfo.size() - 1).getQualifiedName();
        }

        genericSimpleInfo.setSubTypeName(subTypeNames);

        return new PairTuple<>(innerType, lastTypeName);
    }

    private <T> T readField(byte[] input) {
        return KryoBaseUtil.readFromByteArray(input);
    }

    private <T> byte[] convertField(T input) {
        return KryoBaseUtil.writeToByteArray(input);
    }
}
