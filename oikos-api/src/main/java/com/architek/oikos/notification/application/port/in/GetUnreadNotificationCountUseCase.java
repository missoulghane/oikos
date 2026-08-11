package com.architek.oikos.notification.application.port.in;

import com.architek.oikos.notification.application.query.GetUnreadNotificationCountQuery;

public interface GetUnreadNotificationCountUseCase {

    long getUnreadCount(GetUnreadNotificationCountQuery query);
}
