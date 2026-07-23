package com.architek.oikos.installment.web.response;

import java.util.List;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedInstallmentResponse(List<InstallmentResponse> content, int pageNumber, int pageSize,
                                           long totalElements, int totalPages) {

    public static PagedInstallmentResponse from(Page<InstallmentView> page) {
        List<InstallmentResponse> content = page.content().stream().map(InstallmentResponse::from).toList();
        return new PagedInstallmentResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
