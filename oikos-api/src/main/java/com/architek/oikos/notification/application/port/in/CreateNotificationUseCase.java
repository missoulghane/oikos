package com.architek.oikos.notification.application.port.in;

import com.architek.oikos.notification.application.command.CreateNotificationCommand;
import com.architek.oikos.notification.domain.valueobject.NotificationId;

public interface CreateNotificationUseCase {

    NotificationId create(CreateNotificationCommand command);
}
