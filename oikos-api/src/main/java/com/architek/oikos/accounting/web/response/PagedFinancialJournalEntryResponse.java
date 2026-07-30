package com.architek.oikos.accounting.web.response;

import java.util.List;

import com.architek.oikos.accounting.application.dto.FinancialJournalEntryView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedFinancialJournalEntryResponse(List<FinancialJournalEntryResponse> content, int pageNumber,
                                                  int pageSize, long totalElements, int totalPages) {

    public static PagedFinancialJournalEntryResponse from(Page<FinancialJournalEntryView> page) {
        List<FinancialJournalEntryResponse> content = page.content().stream()
                .map(FinancialJournalEntryResponse::from).toList();
        return new PagedFinancialJournalEntryResponse(content, page.pageNumber(), page.pageSize(),
                page.totalElements(), page.totalPages());
    }
}
