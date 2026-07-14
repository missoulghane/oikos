package com.architek.oikos.property.web.response;

import java.util.List;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedUnitResponse(List<UnitResponse> content, int pageNumber, int pageSize,
                                long totalElements, int totalPages) {

    public static PagedUnitResponse from(Page<UnitView> page) {
        List<UnitResponse> content = page.content().stream().map(UnitResponse::from).toList();
        return new PagedUnitResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
