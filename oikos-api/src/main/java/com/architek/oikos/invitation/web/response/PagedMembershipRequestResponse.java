package com.architek.oikos.invitation.web.response;

import java.util.List;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedMembershipRequestResponse(List<MembershipRequestResponse> content, int pageNumber, int pageSize,
                                              long totalElements, int totalPages) {

    public static PagedMembershipRequestResponse from(Page<MembershipRequestOverviewView> page) {
        List<MembershipRequestResponse> content = page.content().stream().map(MembershipRequestResponse::from).toList();
        return new PagedMembershipRequestResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
