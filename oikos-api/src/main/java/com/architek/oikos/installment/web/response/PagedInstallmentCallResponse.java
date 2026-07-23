package com.architek.oikos.installment.web.response;

import java.util.List;

import com.architek.oikos.installment.application.dto.InstallmentCallSummaryView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedInstallmentCallResponse(List<InstallmentCallSummaryResponse> content, int pageNumber, int pageSize,
                                              long totalElements, int totalPages) {

    public static PagedInstallmentCallResponse from(Page<InstallmentCallSummaryView> page) {
        List<InstallmentCallSummaryResponse> content = page.content().stream()
                .map(InstallmentCallSummaryResponse::from).toList();
        return new PagedInstallmentCallResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
