package com.architek.oikos.property.web.response;

import java.util.List;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedPropertyContactResponse(List<PropertyContactResponse> content, int pageNumber, int pageSize,
                                            long totalElements, int totalPages) {

    public static PagedPropertyContactResponse from(Page<PropertyContactView> page) {
        List<PropertyContactResponse> content = page.content().stream().map(PropertyContactResponse::from).toList();
        return new PagedPropertyContactResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(),
                page.totalPages());
    }
}
