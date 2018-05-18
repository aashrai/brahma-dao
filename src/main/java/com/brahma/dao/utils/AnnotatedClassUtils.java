package com.brahma.dao.utils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class AnnotatedClassUtils {

    public static String getQualifiedClassName(TypeElement typeElement, Elements elementUtils) {
        return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    public static String getPackageOf(TypeElement typeElement, Elements elementUtils) {
        return elementUtils.getPackageOf(typeElement).toString();
    }

    public static String getSimpleName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString();
    }

    public static String getParameterName(TypeElement typeElement) {
        String simpleName = typeElement.getSimpleName().toString();
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length());
    }



}
