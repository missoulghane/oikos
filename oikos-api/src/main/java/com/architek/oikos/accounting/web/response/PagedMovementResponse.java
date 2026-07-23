package com.architek.oikos.accounting.web.response;

import java.util.List;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedMovementResponse(List<MovementResponse> content, int pageNumber, int pageSize,
                                        long totalElements, int totalPages) {

    public static PagedMovementResponse from(Page<MovementView> page) {
        List<MovementResponse> content = page.content().stream().map(MovementResponse::from).toList();
        return new PagedMovementResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
