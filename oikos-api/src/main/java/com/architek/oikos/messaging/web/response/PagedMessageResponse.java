package com.architek.oikos.messaging.web.response;

import java.util.List;

import com.architek.oikos.messaging.application.dto.MessageView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedMessageResponse(List<MessageResponse> content, int pageNumber, int pageSize, long totalElements,
                                    int totalPages) {

    public static PagedMessageResponse from(Page<MessageView> page) {
        List<MessageResponse> content = page.content().stream().map(MessageResponse::from).toList();
        return new PagedMessageResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
