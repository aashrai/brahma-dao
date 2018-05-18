package com.brahma.dao.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMeta {
    private String sortBy;
    private SortType sortType;
    private Integer maxResults;
    private Integer firstResult;

}
