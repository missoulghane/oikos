package com.architek.oikos.invitation.web.response;

import java.util.List;

import com.architek.oikos.invitation.application.port.out.AvailableUnitInfo;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedAvailableUnitResponse(List<AvailableUnitResponse> content, int pageNumber, int pageSize,
                                          long totalElements, int totalPages) {

    public static PagedAvailableUnitResponse from(Page<AvailableUnitInfo> page) {
        List<AvailableUnitResponse> content = page.content().stream().map(AvailableUnitResponse::from).toList();
        return new PagedAvailableUnitResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
