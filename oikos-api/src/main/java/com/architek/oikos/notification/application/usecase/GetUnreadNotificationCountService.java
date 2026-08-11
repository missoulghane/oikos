package com.architek.oikos.notification.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.notification.application.port.in.GetUnreadNotificationCountUseCase;
import com.architek.oikos.notification.application.query.GetUnreadNotificationCountQuery;
import com.architek.oikos.notification.domain.repository.NotificationRepository;

@Component
public class GetUnreadNotificationCountService implements GetUnreadNotificationCountUseCase {

    private final NotificationRepository notificationRepository;

    public GetUnreadNotificationCountService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(GetUnreadNotificationCountQuery query) {
        return notificationRepository.countUnreadByRecipientUserId(query.userId());
    }
}
