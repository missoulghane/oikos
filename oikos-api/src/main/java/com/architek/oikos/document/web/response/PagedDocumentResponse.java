package com.architek.oikos.document.web.response;

import java.util.List;

import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedDocumentResponse(List<DocumentResponse> content, int pageNumber, int pageSize,
                                     long totalElements, int totalPages) {

    public static PagedDocumentResponse from(Page<DocumentView> page) {
        List<DocumentResponse> content = page.content().stream().map(DocumentResponse::from).toList();
        return new PagedDocumentResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(),
                page.totalPages());
    }
}
