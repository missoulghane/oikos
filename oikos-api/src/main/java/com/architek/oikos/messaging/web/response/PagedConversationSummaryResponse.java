package com.architek.oikos.messaging.web.response;

import java.util.List;

import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedConversationSummaryResponse(List<ConversationSummaryResponse> content, int pageNumber, int pageSize,
                                                long totalElements, int totalPages) {

    public static PagedConversationSummaryResponse from(Page<ConversationSummaryView> page) {
        List<ConversationSummaryResponse> content = page.content().stream().map(ConversationSummaryResponse::from).toList();
        return new PagedConversationSummaryResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
