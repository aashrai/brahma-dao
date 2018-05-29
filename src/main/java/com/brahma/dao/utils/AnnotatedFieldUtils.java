/*
 * Copyright (c) 2018 gozefo.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *SOFTWARE
 */
package com.brahma.dao.utils;

import com.brahma.dao.annotations.Default;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;

/**
 * Contains utility methods for processing the fields in the annotated element.
 */
public final class AnnotatedFieldUtils {
    /**
     *
     */
    private AnnotatedFieldUtils() {

    }

    /**
     * @param field Field of the annotated element/class.
     * @return Returns type of that particular field.
     */
    public static TypeName getCastType(final Element field) {

        return TypeName.get(field.asType());


    }

    /**
     * @param field Field of the annotated element/class.
     * @return Returns Qualified class name of clazz {@linkplain Default#clazz()}  provided with Default {@linkplain
     * Default} annotation.
     */
    public static String getClassType(final Element field) {
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

    /**
     * @param field Field of the annotated element.
     * @return Returns name of the getter method for a particular field.
     */
    public static String getMethod(final Element field) {
        String fieldName = field.getSimpleName().toString();
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length
                ()) + "()";

    }

    /**
     * @param field Field of the annotated element.
     * @return Returns name of the setter method for a particular field.
     */
    public static String setMethod(final Element field) {
        String fieldName = field.getSimpleName().toString();
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());

    }

    /**
     * @param field Field of the annotated element.
     * @return Returns the value {@linkplain Default#value()} of the Default {@linkplain Default} annotation.
     */
    public static String getDefaultValue(final Element field) {
        if (field.getAnnotation(Default.class) != null) {
            Default annotation = field.getAnnotation(Default.class);
            return annotation.value();
        }
        return null;
    }


}
