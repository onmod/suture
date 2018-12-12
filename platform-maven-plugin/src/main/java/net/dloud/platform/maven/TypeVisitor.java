package net.dloud.platform.maven;

import net.dloud.platform.common.gateway.info.AnnotationInfo;
import net.dloud.platform.common.gateway.info.TypeInfo;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.UnionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static net.dloud.platform.common.serialize.InnerTypeUtil.primitiveName;
import static net.dloud.platform.maven.ParseUtil.fullName;
import static net.dloud.platform.maven.ParseUtil.qualifiedName;
import static net.dloud.platform.maven.ParseUtil.simpleName;


/**
 * @author QuDasheng
 * @create 2018-09-03 20:24
 **/
public class TypeVisitor extends ASTVisitor {
    private String arraySuffix = "[]";
    private Set<String> infoImport = new HashSet<>();

    private Stack<TypeInfo> infoStack = new Stack<>();
    private TypeInfo typeInfo = new TypeInfo();
    private AnnotationInfo annotationInfo = new AnnotationInfo();

    @Override
    public boolean visit(SimpleType node) {
        doType(doVisit(node));
        return true;
    }

    private TypeInfo doVisit(SimpleType node) {
        final TypeInfo typeInfo = new TypeInfo();
        final String simpleName = simpleName(node.getName().getFullyQualifiedName());
        final String qualifiedName = qualifiedName(simpleName);
        typeInfo.setSimpleName(simpleName);
        typeInfo.setQualifiedName(qualifiedName);
        infoImport.add(qualifiedName);
        return typeInfo;
    }

    @Override
    public boolean visit(NameQualifiedType node) {
        doType(doVisit(node));
        return true;
    }

    private TypeInfo doVisit(NameQualifiedType node) {
        final TypeInfo typeInfo = new TypeInfo();
        final String qualifiedName = node.getName().getFullyQualifiedName();
        typeInfo.setSimpleName(simpleName(qualifiedName));
        typeInfo.setQualifiedName(qualifiedName);
        infoImport.add(qualifiedName);
        return typeInfo;
    }

    @Override
    public boolean visit(PrimitiveType node) {
        typeInfo.setIfPrimitive(true);
        doType(doVisit(node));
        return true;
    }

    private TypeInfo doVisit(PrimitiveType node) {
        final TypeInfo typeInfo = new TypeInfo();
        final String simpleName = node.getPrimitiveTypeCode().toString();
        typeInfo.setSimpleName(primitiveName(simpleName));
        typeInfo.setQualifiedName(simpleName);
        typeInfo.setIfPrimitive(true);
        return typeInfo;
    }

    @Override
    public boolean visit(ArrayType node) {
        typeInfo.setIfArray(true);
        final int size = node.getDimensions();
        if (size > 1) {
            final StringBuilder builder = new StringBuilder(arraySuffix);
            for (int i = 1; i < size; i++) {
                builder.append(arraySuffix);
            }
            arraySuffix = builder.toString();
        }
        return true;
    }

    @Override
    public boolean visit(UnionType node) {
        return false;
    }

    @Override
    public boolean visit(ParameterizedType node) {
        final int size = node.typeArguments().size() + 1;
        if (typeInfo.getIfGeneric()) {
            final TypeInfo popInfo = infoStack.pop();
            final TypeInfo newInfo = new TypeInfo();
            newInfo.setIfGeneric(true);
            popInfo.getGenericInfo().add(newInfo);
            for (int i = 0; i < size; i++) {
                infoStack.push(newInfo);
            }
        } else {
            typeInfo.setIfGeneric(true);
            for (int i = 0; i < size; i++) {
                infoStack.push(typeInfo);
            }
        }
        return true;
    }


    /**
     * 没有值的
     */
    @Override
    public boolean visit(MarkerAnnotation node) {
        annotationInfo = annotationBase(node);
        return true;
    }

    /**
     * 单个值的
     */
    @Override
    public boolean visit(SingleMemberAnnotation node) {
        annotationInfo = annotationOne(node);
        final ValueVisitor valueVisitor = new ValueVisitor();
        node.getValue().accept(valueVisitor);
        if (null == valueVisitor.getValue()) {
            annotationInfo.getAnnotationValue().put("value", String.valueOf(node.getValue()));
        } else {
            annotationInfo.getAnnotationValue().put("value", valueVisitor.getValue());
        }
        return true;
    }

    /**
     * 正常的带等号的
     */
    @Override
    public boolean visit(NormalAnnotation node) {
        annotationInfo = annotationOne(node);
        final Map<String, Object> annotationValue = annotationInfo.getAnnotationValue();
        for (Object obj : node.values()) {
            final MemberValuePair pair = (MemberValuePair) obj;
            final ValueVisitor valueVisitor = new ValueVisitor();
            pair.getValue().accept(valueVisitor);
            if (null == valueVisitor.getValue()) {
                annotationValue.put(pair.getName().getFullyQualifiedName(), String.valueOf(pair.getValue()));
            } else {
                annotationValue.put(pair.getName().getFullyQualifiedName(), valueVisitor.getValue());
            }
        }
        return true;
    }

    private void doType(TypeInfo nowInfo) {
        if (typeInfo.getIfGeneric()) {
            final TypeInfo typeInfo = infoStack.pop();
            List<TypeInfo> genericInfo = typeInfo.getGenericInfo();
            if (null == genericInfo) {
                typeInfo.setSimpleName(nowInfo.getSimpleName());
                typeInfo.setQualifiedName(nowInfo.getQualifiedName());
                genericInfo = new ArrayList<>();
            } else {
                genericInfo.add(nowInfo);
            }
            typeInfo.setGenericInfo(genericInfo);
        } else if (typeInfo.getIfArray()) {
            typeInfo.setSimpleName("List");
            if (typeInfo.getIfPrimitive()) {
                typeInfo.setQualifiedName(nowInfo.getQualifiedName() + arraySuffix);
            } else {
                typeInfo.setQualifiedName(qualifiedName(nowInfo.getSimpleName()) + arraySuffix);
            }
        } else {
            if (typeInfo.getIfPrimitive()) {
                typeInfo.setSimpleName(primitiveName(nowInfo.getSimpleName()));
            }
            typeInfo = nowInfo;
        }
    }

    /**
     * 获取注解基本信息
     */
    private AnnotationInfo annotationBase(Annotation node) {
        final AnnotationInfo annotationInfo = new AnnotationInfo();
        final String typeName = node.getTypeName().getFullyQualifiedName();
        annotationInfo.setSimpleName(typeName);
        annotationInfo.setQualifiedName(qualifiedName(typeName));
        return annotationInfo;
    }

    /**
     * 初始化注解值信息
     */
    private AnnotationInfo annotationOne(Annotation node) {
        final AnnotationInfo annotationInfo = annotationBase(node);
        Map<String, Object> annotationValue = annotationInfo.getAnnotationValue();
        if (null == annotationValue) {
            annotationValue = new LinkedHashMap<>();
        }
        annotationInfo.setAnnotationValue(annotationValue);
        return annotationInfo;
    }

    public Set<String> getInfoImport() {
        return infoImport;
    }

    public TypeInfo getTypeInfo() {
        fullName(typeInfo);
        return typeInfo;
    }

    public AnnotationInfo getAnnotationInfo() {
        return annotationInfo;
    }
}
