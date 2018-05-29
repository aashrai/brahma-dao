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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Generates a dao class for a specified entity class. Entity class to be used {@linkplain javax.persistence.Entity}
 * <pre><code>
 * {@literal @}Entity
 * {@literal @}GenerateDao
 * public class EntityClass {}
 * </code></pre>
 *
 * @see "/testfiles/generatedao_input6.txt"
 */

@Target(TYPE)
@Retention(SOURCE)
public @interface GenerateDao {
    /**
     * @return boolean value which generates dao classes with or without search variations. {@linkplain
     * com.brahma.dao.processor.DaoProcessor #generateAllSearchVariationsMethods()}
     * <pre><code>
     * {@literal @}Entity
     * {@literal @}GenerateDao(supportSearchVariations = true)
     * public class EntityClass {}
     * </code></pre>
     * @see "/testfiles/generatedao_input1.txt"
     */
    boolean supportSearchVariations() default false;
}

