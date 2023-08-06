package dev.oop778.bindings.util;

public class ObjectTypeUtility {

    public static String get(Object object) {
        final String simpleName = object.getClass().getSimpleName();
        if (!simpleName.isEmpty()) {
            return simpleName;
        }

        final String[] split = object.getClass().getTypeName().split("\\.");
        return split[split.length - 1];
    }
}
