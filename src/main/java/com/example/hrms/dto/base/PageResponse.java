package com.example.hrms.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private Long total;
    private Integer totalPages;
    private Integer currentPage;
    private Integer size;
    private List<T> content;
}

