package com.architek.oikos.meeting.web.response;

import java.util.List;

import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedGeneralMeetingResponse(List<GeneralMeetingResponse> content, int pageNumber, int pageSize,
                                           long totalElements, int totalPages) {

    public static PagedGeneralMeetingResponse from(Page<GeneralMeetingView> page) {
        List<GeneralMeetingResponse> content = page.content().stream().map(GeneralMeetingResponse::from).toList();
        return new PagedGeneralMeetingResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(),
                page.totalPages());
    }
}
