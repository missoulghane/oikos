package com.architek.oikos.messaging.web.response;

import java.util.List;

import com.architek.oikos.messaging.application.dto.MessageDraftSummaryView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedMessageDraftSummaryResponse(List<MessageDraftSummaryResponse> content, int pageNumber, int pageSize,
                                                long totalElements, int totalPages) {

    public static PagedMessageDraftSummaryResponse from(Page<MessageDraftSummaryView> page) {
        List<MessageDraftSummaryResponse> content = page.content().stream().map(MessageDraftSummaryResponse::from).toList();
        return new PagedMessageDraftSummaryResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(),
                page.totalPages());
    }
}
