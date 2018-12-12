package net.dloud.platform.maven;

import net.dloud.platform.common.gateway.info.AnnotationInfo;
import net.dloud.platform.common.gateway.info.ClassInfo;
import net.dloud.platform.common.gateway.info.CommentInfo;
import net.dloud.platform.common.gateway.info.FieldInfo;
import net.dloud.platform.common.gateway.info.InjectionInfo;
import net.dloud.platform.common.gateway.info.MethodInfo;
import net.dloud.platform.common.gateway.info.ParameterInfo;
import net.dloud.platform.common.gateway.info.PermissionInfo;
import net.dloud.platform.common.gateway.info.TypeInfo;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static net.dloud.platform.common.serialize.InnerTypeUtil.innerClass;
import static net.dloud.platform.maven.InjectionUtil.getInjectionEnum;
import static net.dloud.platform.maven.ParseUtil.addImport;
import static net.dloud.platform.maven.ParseUtil.annotationSet;
import static net.dloud.platform.maven.ParseUtil.classSuffix;
import static net.dloud.platform.maven.ParseUtil.firstLowerCase;
import static net.dloud.platform.maven.ParseUtil.importClass;
import static net.dloud.platform.maven.ParseUtil.prefixName;
import static net.dloud.platform.maven.ParseUtil.qualifiedName;
import static net.dloud.platform.maven.ParseUtil.simpleName;
import static net.dloud.platform.maven.ParseUtil.splitLastByDot;
import static net.dloud.platform.maven.ParseUtil.tag2List;
import static net.dloud.platform.maven.ParseUtil.tag2Text;

/**
 * @author QuDasheng
 * @create 2018-09-03 15:24
 **/
public class ParseVisitor extends ASTVisitor {
    /**
     * 1不包含字段 2不包含方法
     */
    private int parseType = 1;
    private String systemName;
    private String injectionType;
    private String injectionEnum;
    private String permissionType;
    private String whitelistType;
    private String backgroundType;
    private String enquireType;

    private Set<String> filterSuffix;
    private Set<String> fliedFilter;

    private String packageName = "";
    private boolean useAnnotation;

    private Set<String> infoImport = new HashSet<>();
    private List<ClassInfo> sourceInfo = new ArrayList<>();
    private Stack<ClassInfo> infoStack = new Stack<>();
    private PermissionInfo permissionInfo = null;

    private boolean isEnum = false;
    private boolean isInterface = false;
    private boolean isPrimitive = false;
    private boolean isWhitelist = false;
    private boolean isBackground = false;


    public ParseVisitor(String systemName, String injectionType, String injectionEnum,
                        String permissionType, String whitelistType, String backgroundType, String enquireType,
                        boolean useAnnotation, Set<String> filterSuffix, Set<String> fliedFilter) {
        this.systemName = systemName;
        this.injectionType = injectionType;
        this.injectionEnum = injectionEnum;
        this.permissionType = permissionType;
        this.whitelistType = whitelistType;
        this.backgroundType = backgroundType;
        this.enquireType = enquireType;
        this.useAnnotation = useAnnotation;
        this.filterSuffix = filterSuffix;
        this.fliedFilter = fliedFilter;
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        packageName = node.getName().getFullyQualifiedName();
        prefixName = packageName;
        return true;
    }

    @Override
    public boolean visit(ImportDeclaration node) {
        final Name nodeName = node.getName();
        final String fullName = nodeName.getFullyQualifiedName();
        if (fullName.contains("*")) {
            throw new RuntimeException("不允许*号导入");
        }
        if (!node.isStatic() && !fullName.startsWith("java.") && !fullName.startsWith("javax.")
                && !fullName.startsWith("lombok.") && !filterSuffix.contains(classSuffix(fullName))) {
            infoImport.add(fullName);
        }
        importClass.put(simpleName(fullName), fullName);
        return true;
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        isEnum = true;
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        final ClassInfo classInfo = new ClassInfo();
        final String className = node.getName().getFullyQualifiedName();
        if (filterSuffix.contains(classSuffix(className))) {
            return false;
        }
        sourceInfo.add(classInfo);

        //按长度依次入栈
        if (node.isInterface()) {
            //是接口只解析方法
            parseType = 1;
            isInterface = true;
            classInfo.setIfInterface(true);
            for (int i = 0; i < node.getMethods().length; i++) {
                infoStack.push(classInfo);
            }
        } else {
            //否则只解析字段
            parseType = 2;
            for (int i = 0; i < node.getFields().length; i++) {
                infoStack.push(classInfo);
            }
        }

        if (node.isPackageMemberTypeDeclaration()) {
            final String qualifiedName = packageName + "." + className;
            classInfo.setSimpleName(className);
            classInfo.setQualifiedName(qualifiedName);
            importClass.put(className, qualifiedName);
        } else if (node.isMemberTypeDeclaration()) {
            final TypeDeclaration parent = (TypeDeclaration) node.getParent();
            final String parentName = parent.getName().getFullyQualifiedName();
            final String qualifiedName = qualifiedName(parentName) + "." + className;
            final String simpleName = simpleName(parentName) + "." + className;
            classInfo.setSimpleName(simpleName);
            classInfo.setQualifiedName(qualifiedName);
            classInfo.setIfMember(true);
            importClass.put(simpleName, qualifiedName);
        } else {
            classInfo.setQualifiedName(className);
        }

        //设定范型定义信息
        final List typeParameters = node.typeParameters();
        if (null != typeParameters && !typeParameters.isEmpty()) {
            final List<String> generics = new ArrayList<>();
            classInfo.setIfGeneric(true);
            for (Object typeParameter : typeParameters) {
                generics.add(((TypeParameter) typeParameter).getName().getFullyQualifiedName());
            }
            classInfo.setGenericList(generics);
        }

        //只读取一级父类
        final Set<TypeInfo> superclasses = new HashSet<>();
        final Type superclass = node.getSuperclassType();
        if (null != superclass) {
            final TypeVisitor typeVisitor = new TypeVisitor();
            superclass.accept(typeVisitor);
            final TypeInfo typeInfo = typeVisitor.getTypeInfo();
            superclasses.add(typeInfo);
            //防止同包不存在
            addImport(typeInfo.getQualifiedName(), infoImport);
        }
        classInfo.setSuperclass(superclasses);

        //可能存在的注解
        final AnnotationInfoTuple annotationInfoTuple = annotationInfo(node.modifiers());
        if (annotationInfoTuple.ifWhitelist) {
            isWhitelist = true;
        }
        if (annotationInfoTuple.ifBackground) {
            isBackground = true;
        }
        if (null != annotationInfoTuple.permissionInfo) {
            permissionInfo = annotationInfoTuple.permissionInfo;
        }
        classInfo.setAnnotationInfo(annotationInfoTuple.annotationInfos);
        //可能存在的注释
        classInfo.setCommentInfo(commentInfo(node.getJavadoc()));
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (parseType == 1) {
            final ClassInfo sourceInfo = infoStack.pop();
            if (sourceInfo.getIfMember()) {
                prefixName = sourceInfo.getQualifiedName();
            }

            final String methodName = node.getName().getFullyQualifiedName();
            final Type returnType = node.getReturnType2();
            if (null == returnType) {
                return true;
            }

            List<MethodInfo> methodInfos = sourceInfo.getMethodInfo();
            if (null == methodInfos) {
                methodInfos = new ArrayList<>();
            }

            final MethodInfo methodInfo = new MethodInfo();
            methodInfo.setSimpleName(methodName);
            methodInfo.setInvokeName(systemName + "." + firstLowerCase(sourceInfo.getSimpleName()) + "." + methodName);
            Map<String, ParameterInfo> parameterInfos = new LinkedHashMap<>();
            for (Object param : node.parameters()) {
                final SingleVariableDeclaration variable = (SingleVariableDeclaration) param;
                final ParameterInfo parameterInfo = new ParameterInfo();
                //可能存在的注解
                final AnnotationInfoTuple annotationInfoPair = annotationParam(variable.modifiers());
                parameterInfo.setIfInjection(annotationInfoPair.ifInjection);
                if (null != annotationInfoPair.injectionInfo) {
                    parameterInfo.setInjectionInfo(annotationInfoPair.injectionInfo);
                }
                parameterInfo.setAnnotationInfo(annotationInfoPair.annotationInfos);
                parameterInfo.setParamName(variable.getName().getFullyQualifiedName());
                final Type paramType = variable.getType();
                final TypeVisitor paramTypeVisitor = new TypeVisitor();
                paramType.accept(paramTypeVisitor);
                final TypeInfo paramTypeInfo = paramTypeVisitor.getTypeInfo();
                parameterInfo.setParamType(paramTypeInfo);
                parameterInfos.put(parameterInfo.getParamName(), parameterInfo);
                //防止同包不存在
                addImport(paramTypeInfo.getQualifiedName(), infoImport);
                addImport(paramTypeVisitor.getInfoImport(), infoImport);
            }
            methodInfo.setParameterInfo(parameterInfos);
            final TypeVisitor returnTypeVisitor = new TypeVisitor();
            returnType.accept(returnTypeVisitor);
            final TypeInfo returnTypeInfo = returnTypeVisitor.getTypeInfo();
            methodInfo.setReturnInfo(returnTypeInfo);
            //防止同包不存在
            addImport(returnTypeInfo.getQualifiedName(), infoImport);
            addImport(returnTypeVisitor.getInfoImport(), infoImport);
            //可能存在的注解
            final AnnotationInfoTuple annotationInfoTuple = annotationInfo(node.modifiers());
            methodInfo.setAnnotationInfo(annotationInfoTuple.annotationInfos);
            if (isWhitelist || annotationInfoTuple.ifWhitelist) {
                methodInfo.setIfWhitelist(true);
            }
            if (isBackground || annotationInfoTuple.ifBackground) {
                methodInfo.setIfBackground(true);
            }
            final PermissionInfo permissionInfo = annotationInfoTuple.permissionInfo;
            if (null != this.permissionInfo || null != permissionInfo) {
                methodInfo.setPermissionInfo(null == permissionInfo ? this.permissionInfo : permissionInfo);
            }

            //可能存在的注释
            methodInfo.setCommentInfo(commentInfo(node.getJavadoc()));
            methodInfos.add(methodInfo);
            sourceInfo.setMethodInfo(methodInfos);
        }
        return true;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        if (parseType == 2) {
            final ClassInfo sourceInfo = infoStack.pop();
            if (sourceInfo.getIfMember()) {
                prefixName = sourceInfo.getQualifiedName();
            }

            List<FieldInfo> fieldInfos = sourceInfo.getFieldInfo();
            if (null == fieldInfos) {
                fieldInfos = new ArrayList<>();
            }

            final TypeVisitor fieldTypeVisitor = new TypeVisitor();
            node.getType().accept(fieldTypeVisitor);
            final TypeInfo typeInfo = fieldTypeVisitor.getTypeInfo();
            final AnnotationInfoTuple annotationInfoTuple = annotationInfo(node.modifiers());
            final CommentInfo commentInfo = commentInfo(node.getJavadoc());
            for (Object one : node.fragments()) {
                final VariableDeclarationFragment variable = (VariableDeclarationFragment) one;
                final String fieldName = variable.getName().getFullyQualifiedName();
                //过滤掉不应该展示的字段
                if (!fliedFilter.contains(fieldName)) {
                    final FieldInfo fieldInfo = new FieldInfo();
                    fieldInfo.setFieldName(fieldName);
                    fieldInfo.setTypeInfo(typeInfo);
                    //可能存在的注解
                    fieldInfo.setAnnotationInfo(annotationInfoTuple.annotationInfos);
                    //可能存在的注释
                    fieldInfo.setIfEnquire(annotationInfoTuple.ifEnquire);
                    fieldInfo.setCommentInfo(commentInfo);
                    fieldInfos.add(fieldInfo);
                }
            }
            //防止同包不存在
            addImport(typeInfo.getQualifiedName(), infoImport);
            addImport(fieldTypeVisitor.getInfoImport(), infoImport);

            if (sourceInfo.getIfPrimitive() && !typeInfo.getIfPrimitive()
                    && !infoImport.isEmpty() && !innerClass.containsKey(typeInfo.getSimpleName())) {
                sourceInfo.setIfPrimitive(false);
            }
            sourceInfo.setFieldInfo(fieldInfos);
        }
        return true;
    }

    private CommentInfo commentInfo(Javadoc docs) {
        if (null == docs || null == docs.tags()) {
            return null;
        }
        final CommentInfo comment = new CommentInfo();
        for (Object tag : docs.tags()) {
            final TagElement tagElement = (TagElement) tag;
            //注释标签名
            if (null == tagElement.getTagName()) {
                //说明
                comment.setDetail(tag2Text(tagElement));
            } else {
                switch (tagElement.getTagName().toLowerCase().trim()) {
                    case "@author":
                        //作者
                        comment.setAuthor(tag2Text(tagElement));
                        break;
                    case "@title":
                        //标题
                        comment.setTitle(tag2Text(tagElement));
                        break;
                    case "@time":
                        //时间
                        comment.setTime(tag2Text(tagElement));
                        break;
                    case TagElement.TAG_PARAM:
                        Map<String, String> param = comment.getParams();
                        if (null == param) {
                            param = new LinkedHashMap<>();
                        }
                        final List<String> paramList = tag2List(tagElement);
                        if (paramList.size() % 2 == 0) {
                            for (int i = 0; i < paramList.size() - 1; i += 2) {
                                param.put(paramList.get(i), paramList.get(i + 1));
                            }
                        } else {
                            param.put(paramList.get(0), "");
                        }
                        comment.setParams(param);
                        break;
                    case TagElement.TAG_RETURN:
                        comment.setReturned(tag2Text(tagElement));
                        break;
                    case TagElement.TAG_EXCEPTION:
                        comment.setException(tag2List(tagElement));
                        break;
                    default:
                }
            }
        }
        return comment;
    }

    @SuppressWarnings("unchecked")
    private AnnotationInfoTuple annotationInfo(List list) {
        final AnnotationInfoTuple annotationInfoPair = new AnnotationInfoTuple();
        if (null == list || list.isEmpty()) {
            return annotationInfoPair;
        }

        List<AnnotationInfo> annotationInfos = new ArrayList<>();
        for (Object one : list) {
            if (one instanceof Annotation) {
                final Annotation annotation = (Annotation) one;
                final TypeVisitor typeVisitor = new TypeVisitor();
                annotation.accept(typeVisitor);
                final AnnotationInfo annotationInfo = typeVisitor.getAnnotationInfo();
                if (enquireType.equalsIgnoreCase(annotationInfo.getQualifiedName())) {
                    annotationInfoPair.ifEnquire = true;
                }
                if (whitelistType.equalsIgnoreCase(annotationInfo.getQualifiedName())) {
                    annotationInfoPair.ifWhitelist = true;
                }
                if (backgroundType.equalsIgnoreCase(annotationInfo.getQualifiedName())) {
                    annotationInfoPair.ifBackground = true;
                }
                if (permissionType.equalsIgnoreCase(annotationInfo.getQualifiedName())) {
                    Map<String, Object> annotationValue = annotationInfo.getAnnotationValue();
                    if (null == annotationValue) {
                        annotationValue = new HashMap<>();
                    }

                    final PermissionInfo permissionInfo = new PermissionInfo();
                    final Object gateway = annotationValue.get("value");
                    if (null != gateway) {
                        permissionInfo.setGateway(Boolean.valueOf(String.valueOf(gateway)));
                    }
                    final Object deny = annotationValue.get("deny");
                    if (null != deny) {
                        permissionInfo.setDeny(annotationSet(deny));
                    }
                    final Object allow = annotationValue.get("allow");
                    if (null != allow) {
                        permissionInfo.setAllow(annotationSet(allow));
                    } else {
                        if (null == deny) {
                            final Set<String> set = new HashSet<>();
                            set.add("*");
                            permissionInfo.setAllow(set);
                        }
                    }
                    final Object level = annotationValue.get("level");
                    if (null != level) {
                        permissionInfo.setLevel(Integer.parseInt(String.valueOf(level)));
                    } else {
                        permissionInfo.setLevel(0);
                    }
                    final Object limit = annotationValue.get("limit");
                    if (null != limit) {
                        permissionInfo.setLimit(Integer.parseInt(String.valueOf(limit)));
                    } else {
                        permissionInfo.setLimit(0);
                    }
                    annotationInfoPair.permissionInfo = permissionInfo;
                }
                if (useAnnotation) {
                    annotationInfos.add(annotationInfo);
                }
            }
        }
        if (useAnnotation) {
            annotationInfoPair.annotationInfos = annotationInfos;
        }
        return annotationInfoPair;
    }

    private AnnotationInfoTuple annotationParam(List list) {
        final AnnotationInfoTuple annotationInfoPair = new AnnotationInfoTuple();
        if (null == list || list.isEmpty()) {
            return annotationInfoPair;
        }

        List<AnnotationInfo> annotationInfos = new ArrayList<>();
        for (Object one : list) {
            if (one instanceof Annotation) {
                final Annotation annotation = (Annotation) one;
                final TypeVisitor typeVisitor = new TypeVisitor();
                annotation.accept(typeVisitor);
                final AnnotationInfo annotationInfo = typeVisitor.getAnnotationInfo();
                if (injectionType.equalsIgnoreCase(annotationInfo.getQualifiedName())) {
                    final InjectionInfo injectionInfo = new InjectionInfo();
                    Map<String, Object> annotationValue = annotationInfo.getAnnotationValue();
                    if (null == annotationValue) {
                        annotationValue = new HashMap<>();
                    }

                    final Map<String, String> injectionEnum = getInjectionEnum(this.injectionEnum);
                    final Object value = annotationValue.get("value");
                    if (null == value) {
                        injectionInfo.setInjectType(injectionEnum.getOrDefault(InjectionUtil.DEFAULT_VALUE, ""));
                        injectionInfo.setInvokeName(injectionEnum.get(injectionInfo.getInjectType()));
                    } else {
                        injectionInfo.setInjectType(splitLastByDot(String.valueOf(value)));
                        injectionInfo.setInvokeName(injectionEnum.get(injectionInfo.getInjectType()));
                    }
                    final Object ip = annotationValue.get("ip");
                    if (null != ip) {
                        injectionInfo.setHaveAddress(Boolean.valueOf(String.valueOf(ip)));
                    }
                    final Object headers = annotationValue.get("headers");
                    if (null != headers) {
                        injectionInfo.setHeaderNames(annotationSet(headers));
                    }
                    final Object cookies = annotationValue.get("cookies");
                    if (null != cookies) {
                        injectionInfo.setCookieNames(annotationSet(cookies));
                    }

                    annotationInfoPair.ifInjection = true;
                    annotationInfoPair.injectionInfo = injectionInfo;
                }
                if (useAnnotation) {
                    annotationInfos.add(annotationInfo);
                }
            }
        }
        annotationInfoPair.annotationInfos = annotationInfos;
        return annotationInfoPair;
    }

    public List<ClassInfo> getSourceInfo() {
        infoImport.removeAll(Arrays.asList(injectionType, injectionEnum, permissionType, whitelistType, enquireType));
        for (ClassInfo classInfo : sourceInfo) {
            if (!infoImport.isEmpty()) {
                classInfo.setImportInfo(infoImport);
            }
        }
        return sourceInfo;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public boolean isInterface() {
        return isInterface;
    }

    private static class AnnotationInfoTuple {
        List<AnnotationInfo> annotationInfos;
        PermissionInfo permissionInfo;
        InjectionInfo injectionInfo;
        boolean ifInjection = false;
        boolean ifWhitelist = false;
        boolean ifBackground = false;
        boolean ifEnquire = false;
    }
}
