package com.architek.oikos.contact.web.response;

import java.util.List;

import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedContactResponse(List<ContactResponse> content, int pageNumber, int pageSize, long totalElements,
                                    int totalPages) {

    public static PagedContactResponse from(Page<ContactView> page) {
        List<ContactResponse> content = page.content().stream().map(ContactResponse::from).toList();
        return new PagedContactResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
