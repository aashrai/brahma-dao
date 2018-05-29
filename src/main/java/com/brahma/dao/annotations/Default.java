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

package com.brahma.dao.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Sets a default value of a field. Default value is used while persisting if the field value is null
 * <pre><code>
 * {@literal @}Entity
 * {@literal @}GenerateDao
 * public class EntityClass{
 *      String id;
 *     {@literal @}Default(value = "default") String id;
 * }
 * </code></pre>
 *
 * @see "/testfiles/generatedao_input1.txt"
 * @see "/testfiles/generatedao_output1.txt {#createOrUpdate}"
 */
@Target(ElementType.FIELD)
@Retention(SOURCE)
public @interface Default {
    /**
     * @return default value of a specified field.
     */
    String value();

    /**
     * This needs to be provided only when we need to set default value for a generic enum.
     * <pre><code>
     *     public enum GenericEnum {
     *         DEFAULT,
     *         SPECIFIED;
     *     }
     * </code></pre>
     * Entity class looks like this
     * <pre><code>
     *     {@literal @}Entity
     *     {@literal @}GenerateDao
     *     public class EntityClass {
     *         {@literal @}Default(value = "DEFAULT", clazz = Enum.class {@linkplain Enum})
     *         GenericEnum setDefaultValue;
     *
     *     }
     * </code></pre>
     * To specify the field is enum, clazz value should be "Enum.class" {@linkplain Enum}
     *
     * @return generic enum for which the default value is to be set
     * @see "/testfiles/generatedao_input1.txt"
     */
    Class clazz() default void.class;
}
