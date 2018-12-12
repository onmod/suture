package net.dloud.platform.common.serialize;

import java.util.HashMap;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-16 23:24
 **/
public class InnerTypeUtil {
    public static Map<String, String> innerClass = new HashMap<>();

    static {
        innerClass.put("boolean", "boolean");
        innerClass.put("byte", "byte");
        innerClass.put("short", "short");
        innerClass.put("int", "int");
        innerClass.put("long", "long");
        innerClass.put("double", "double");
        innerClass.put("float", "float");
        innerClass.put("char", "char");

        initImport(new String[]{"Object", "Boolean", "Byte", "Short", "Integer", "Long", "Double", "Float", "Character", "Void",
                "Number", "CharSequence", "Math", "Enum", "Class", "ClassLoader", "ClassValue", "Runtime", "System",
                "ThreadLocal", "ThreadGroup", "Thread", "ThreadDeath", "ThreadLocal", "ThreadGroup",
                "String", "StringBuffer", "StringBuffer", "StringCoding", "StringIndexOutOfBoundsException",
                "Cloneable", "Comparable", "Runnable", "Readable", "Iterable", "AutoCloseable", "Appendable",
                "Error", "Throwable", "Exception", "RuntimeException", "TypeNotPresentException",
                "Deprecated", "SuppressWarnings", "SafeVarargs", "Override", "FunctionalInterface", ""});
    }

    private static void initImport(String[] strings) {
        for (String name : strings) {
            innerClass.put(name, "java.lang." + name);
        }
    }

    public static String primitiveName(String name) {
        switch (name) {
            case "boolean":
                return "Boolean";
            case "byte":
                return "Byte";
            case "short":
                return "Short";
            case "int":
                return "Integer";
            case "long":
                return "Long";
            case "double":
                return "Double";
            case "float":
                return "Float";
            case "char":
                return "Character";
            default:
                return name;
        }
    }

    public static boolean isInnerType(String name) {
        return innerClass.containsValue(name);
    }

    public static boolean isInnerTypeByKey(String name) {
        return innerClass.containsKey(name);
    }

    public static boolean isJavaType(String name) {
        return name.startsWith("java.") || name.startsWith("javax.");
    }

    public static boolean isIntType(String name) {
        return "int".equals(name) || "java.lang.Integer".equals(name);
    }

    public static boolean isLongType(String name) {
        return "long".equals(name) || "java.lang.Long".equals(name);
    }
}
