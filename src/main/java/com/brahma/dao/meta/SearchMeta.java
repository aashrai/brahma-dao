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
package com.brahma.dao.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class provides search parameters for generated dao search methods. Particular page of the result set can be
 * selected using SearchMeta class.
 *
 * {@linkplain com.brahma.dao.utils.CreateDaoUtils #createGetSearchQueryMethodWithParams(ClassName)}"
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMeta {
    /**
     * Provides the attribute/field to sort with.
     */
    private String sortBy;
    /**
     * Provides the sorting type.
     */
    private SortType sortType;
    /**
     * Maximum rows to fetch in a search.
     */
    private Integer maxResults;
    /**
     * Index from the where the first row should be retrieved.
     */
    private Integer firstResult;

}
