package com.architek.oikos.invitation.web.response;

import java.util.List;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedInvitationResponse(List<InvitationResponse> content, int pageNumber, int pageSize,
                                       long totalElements, int totalPages) {

    public static PagedInvitationResponse from(Page<InvitationView> page) {
        List<InvitationResponse> content = page.content().stream().map(InvitationResponse::from).toList();
        return new PagedInvitationResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
