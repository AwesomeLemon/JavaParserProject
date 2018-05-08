package com.github.awesomelemon;

import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedType;

public class Util {
    private Util() {
    }

    static String getShortTypeName(MethodUsage methodUsage) {
        String fullType = methodUsage.returnType().describe();
        return substringAfterLastDot(fullType);
    }

    static String getShortTypeName(ResolvedType resolvedType) {
        String fullType = resolvedType.describe();
        return substringAfterLastDot(fullType);
    }

    static <T extends Type> String getShortTypeName(T type) {
        String fullType = type.asString();
        return substringAfterLastDot(fullType);
    }

    static String getShortTypeName(String fullType) {
        return substringAfterLastDot(fullType);
    }

    private static String substringAfterLastDot(String fullType) {
        return fullType;
//        return fullType.substring(fullType.lastIndexOf('.') + 1);
    }
}
