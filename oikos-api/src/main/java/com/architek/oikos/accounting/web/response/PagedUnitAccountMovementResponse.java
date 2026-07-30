package com.architek.oikos.accounting.web.response;

import java.util.List;

import com.architek.oikos.accounting.application.dto.UnitAccountMovementView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedUnitAccountMovementResponse(List<UnitAccountMovementResponse> content, int pageNumber,
                                                int pageSize, long totalElements, int totalPages) {

    public static PagedUnitAccountMovementResponse from(Page<UnitAccountMovementView> page) {
        List<UnitAccountMovementResponse> content = page.content().stream().map(UnitAccountMovementResponse::from).toList();
        return new PagedUnitAccountMovementResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(),
                page.totalPages());
    }
}
