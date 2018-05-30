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

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Provides Simple and Qualified class names for annotated elements.
 *
 * @author sarthak
 * @since 1.0
 */

public final class AnnotatedClassUtils {
    /**
     *
     */
    private AnnotatedClassUtils() {

    }

    /**
     * @param typeElement  Represents a class or interface annotated element.
     * @param elementUtils Utility methods for operating on annotated elements
     * @return Qualified class name. It is the package name of the the class eg. {com.brahma.dao.utils}
     */

    public static String getQualifiedClassName(final TypeElement typeElement, final Elements elementUtils) {
        return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    /**
     * @param typeElement Represents a class or interface program element.
     * @return Simple class name eg. {AnnotatedClassUtils}
     */
    public static String getSimpleName(final TypeElement typeElement) {
        return typeElement.getSimpleName().toString();
    }

    /**
     * @param typeElement Represents a class or interface program element.
     * @return Returns the object name for a annotated element.
     */
    public static String getParameterName(final TypeElement typeElement) {
        String simpleName = typeElement.getSimpleName().toString();
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length());
    }


}
