package com.architek.oikos.accounting.web.response;

import java.util.List;

import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedExpenseResponse(List<ExpenseResponse> content, int pageNumber, int pageSize, long totalElements,
                                    int totalPages) {

    public static PagedExpenseResponse from(Page<ExpenseView> page) {
        List<ExpenseResponse> content = page.content().stream().map(ExpenseResponse::from).toList();
        return new PagedExpenseResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(),
                page.totalPages());
    }
}
