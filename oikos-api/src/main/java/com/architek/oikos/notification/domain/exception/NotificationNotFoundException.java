package com.architek.oikos.notification.domain.exception;

import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class NotificationNotFoundException extends ResourceNotFoundException {

    public NotificationNotFoundException(NotificationId id) {
        super("Notification not found with id: " + id);
    }
}
