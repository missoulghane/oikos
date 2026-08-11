package com.architek.oikos.notification.domain.repository;

import java.util.Optional;

import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(NotificationId id);

    /** Every notification of the given recipient, most recent first. */
    Page<Notification> findByRecipientUserId(EntityId recipientUserId, PageRequest pageRequest);

    long countUnreadByRecipientUserId(EntityId recipientUserId);
}
