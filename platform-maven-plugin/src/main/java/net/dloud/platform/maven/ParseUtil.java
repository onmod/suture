package net.dloud.platform.maven;

import net.dloud.platform.common.gateway.info.TypeInfo;
import net.dloud.platform.common.serialize.InnerTypeUtil;
import org.eclipse.jdt.core.dom.TagElement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.dloud.platform.common.serialize.InnerTypeUtil.innerClass;

/**
 * @author QuDasheng
 * @create 2018-09-03 20:25
 **/
public class ParseUtil {
    public static String prefixName = "java.lang.";

    public static Map<String, String> importClass = new HashMap<>(innerClass);

    public static String simpleName(String name) {
        if (name.contains(".")) {
            final String[] split = name.split("\\.");
            final String simple = split[split.length - 1];
            importClass.put(simple, name);
            return simple;
        } else {
            return name;
        }
    }

    public static String qualifiedName(String name) {
        return importClass.getOrDefault(name, prefixName + "." + name);
    }

    public static String qualifiedName(String name, String parent) {
        return importClass.getOrDefault(name, parent + "." + name);
    }

    public static void fullName(TypeInfo typeInfo) {
        if (null == typeInfo.getFullName()) {
            final List<TypeInfo> genericInfo = typeInfo.getGenericInfo();
            if (typeInfo.getIfGeneric()) {
                int i = 0;
                int size = genericInfo.size();
                final StringBuilder fullName = new StringBuilder(typeInfo.getQualifiedName());
                for (TypeInfo nextTypeInfo : genericInfo) {
                    fullName(nextTypeInfo);
                    if (i == 0) {
                        fullName.append("<").append(nextTypeInfo.getFullName());
                        if (size == 1) {
                            fullName.append(">");
                        } else {
                            fullName.append(", ");
                        }
                    } else if (i == size - 1) {
                        fullName.append(nextTypeInfo.getFullName()).append(">");
                    } else {
                        fullName.append(nextTypeInfo.getFullName()).append(", ");
                    }
                    i += 1;
                }
                typeInfo.setFullName(fullName.toString());
            } else {
                typeInfo.setFullName(typeInfo.getQualifiedName());
            }
        }
    }

    public static void addImport(String qualifiedName, Set<String> infoImport) {
        if (!InnerTypeUtil.isJavaType(qualifiedName) && !InnerTypeUtil.isInnerType(qualifiedName)) {
            infoImport.add(qualifiedName);
        }
    }

    public static void addImport(Set<String> qualifiedNames, Set<String> infoImport) {
        for (String qualifiedName : qualifiedNames) {
            addImport(qualifiedName, infoImport);
        }
    }

    public static String tag2Text(TagElement tag) {
        return tagList2Text(tag2List(tag));
    }

    public static List<String> tag2List(TagElement tag) {
        final List<String> list = new ArrayList<>();
        //注释具体内容片段，去除空格等
        if (null != tag && null != tag.fragments()) {
            for (Object fragment : tag.fragments()) {
                if (null != fragment) {
                    list.add(fragment.toString().replaceAll("\\s", ""));
                }
            }
        }
        return list;
    }

    public static String tagList2Text(List<String> fragments) {
        if (fragments.size() == 0) {
            return null;
        }
        if (fragments.size() == 1) {
            return fragments.get(0);
        }
        int i = 0;
        final StringBuilder sb = new StringBuilder();
        for (String fragment : fragments) {
            if (null != fragment) {
                if (i == fragments.size() - 1) {
                    sb.append(fragment);
                } else {
                    sb.append(fragment).append("<br />");
                }
            }
            i += 1;
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> annotationSet(Object value) {
        if (null != value) {
            if (value instanceof Collection) {
                return new HashSet<>((Collection<T>) value);
            } else {
                final Set<T> set = new HashSet<>();
                set.add((T) value);
                return set;
            }
        }
        return null;
    }

    /**
     * 获取类的后缀
     *
     * @param name
     * @return
     */
    public static String classSuffix(String name) {
        final String[] split = name.split("[A-Z]");
        final int length = split.length;
        final int sub = name.length() - split[length - 1].length();
        if (length > 1 && sub > 1) {
            return name.substring(sub - 1);
        } else {
            return name;
        }
    }

    /**
     * 首字母大写
     *
     * @param str 字符串
     * @return
     */
    public static String firstLowerCase(String str) {
        final String strFirst = String.valueOf(str.charAt(0));
        return str.replaceFirst(strFirst, strFirst.toLowerCase());
    }

    /**
     * 按点分割并返回最后一个
     *
     * @param str
     * @return
     */
    public static String splitLastByDot(String str) {
        if (null == str) {
            return "";
        }

        String res = "";
        String[] split = str.split("\\.");
        final int length = split.length;
        if (length > 0) {
            res = split[length - 1];
        }

        if (null == res) {
            return "";
        } else {
            return res;
        }
    }

    public static String getVersion(byte[] data) {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        }
        sha.update(data);
        byte[] resultBytes = sha.digest();

        return bytesToHex(resultBytes);
    }

    public static String bytesToHex(byte[] input) {
        final StringBuilder builder = new StringBuilder();
        for (final byte b : input) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
