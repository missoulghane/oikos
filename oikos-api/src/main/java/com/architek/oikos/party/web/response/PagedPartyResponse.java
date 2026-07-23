package com.architek.oikos.party.web.response;

import java.util.List;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedPartyResponse(List<PartyResponse> content, int pageNumber, int pageSize, long totalElements,
                                    int totalPages) {

    public static PagedPartyResponse from(Page<PartyView> page) {
        List<PartyResponse> content = page.content().stream().map(PartyResponse::from).toList();
        return new PagedPartyResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
