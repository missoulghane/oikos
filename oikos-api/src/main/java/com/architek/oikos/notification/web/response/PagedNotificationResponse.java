package com.architek.oikos.notification.web.response;

import java.util.List;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.shared.domain.pagination.Page;

public record PagedNotificationResponse(List<NotificationResponse> content, int pageNumber, int pageSize,
                                         long totalElements, int totalPages) {

    public static PagedNotificationResponse from(Page<NotificationView> page) {
        List<NotificationResponse> content = page.content().stream().map(NotificationResponse::from).toList();
        return new PagedNotificationResponse(content, page.pageNumber(), page.pageSize(), page.totalElements(), page.totalPages());
    }
}
