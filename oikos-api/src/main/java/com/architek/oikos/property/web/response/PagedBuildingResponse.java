package com.architek.oikos.property.web.response;

import java.util.List;

import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedBuildingResponse(List<BuildingResponse> content, int pageNumber, int pageSize,
                                     long totalElements, int totalPages) {

    public static PagedBuildingResponse from(Page<BuildingView> page) {
        List<BuildingResponse> content = page.content().stream().map(BuildingResponse::from).toList();
        return new PagedBuildingResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
