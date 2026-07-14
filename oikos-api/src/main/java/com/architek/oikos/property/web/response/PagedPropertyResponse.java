package com.architek.oikos.property.web.response;

import java.util.List;

import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedPropertyResponse(List<PropertyResponse> content, int pageNumber, int pageSize,
                                        long totalElements, int totalPages) {

    public static PagedPropertyResponse from(Page<PropertyView> page) {
        List<PropertyResponse> content = page.content().stream().map(PropertyResponse::from).toList();
        return new PagedPropertyResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
