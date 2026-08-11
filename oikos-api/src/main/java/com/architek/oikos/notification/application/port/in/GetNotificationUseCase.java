package com.architek.oikos.notification.application.port.in;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.query.GetNotificationQuery;

public interface GetNotificationUseCase {

    NotificationView getNotification(GetNotificationQuery query);
}
