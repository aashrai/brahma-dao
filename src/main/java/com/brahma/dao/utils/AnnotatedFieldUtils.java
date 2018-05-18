package com.brahma.dao.utils;

import com.brahma.dao.annotations.Default;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;

public class AnnotatedFieldUtils {


    public static TypeName getCastType(Element field) {

        return TypeName.get(field.asType());


    }

    public static String getClassType(Element field) {
        if (field.getAnnotation(Default.class) != null) {
            Default annotation = field.getAnnotation(Default.class);

            try {
                return annotation.clazz().toString();
            } catch (MirroredTypeException mte) {

                return mte.getTypeMirror().toString();
            }
        }
        return "null";


    }

    public static String getMethod(Element field) {
        String fieldName = field.getSimpleName().toString();
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length
                ()) + "()";

    }

    public static String setMethod(Element field) {
        String fieldName = field.getSimpleName().toString();
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());

    }


    public static String getDefaultValue(Element field) {
        if (field.getAnnotation(Default.class) != null) {
            Default annotation = field.getAnnotation(Default.class);
            return annotation.value();
        }
        return null;
    }


}
