package com.architek.oikos.notification.application.port.in;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.query.ListMyNotificationsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListMyNotificationsUseCase {

    Page<NotificationView> listNotifications(ListMyNotificationsQuery query);
}
