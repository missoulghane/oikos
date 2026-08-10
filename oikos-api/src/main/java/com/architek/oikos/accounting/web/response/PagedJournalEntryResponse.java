package com.architek.oikos.accounting.web.response;

import java.util.List;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedJournalEntryResponse(List<JournalEntryResponse> content, int pageNumber, int pageSize,
                                         long totalElements, int totalPages) {

    public static PagedJournalEntryResponse from(Page<JournalEntryView> page) {
        List<JournalEntryResponse> content = page.content().stream().map(JournalEntryResponse::from).toList();
        return new PagedJournalEntryResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(),
                page.totalPages());
    }
}
