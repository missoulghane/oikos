package com.architek.oikos.notification.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.port.in.GetNotificationUseCase;
import com.architek.oikos.notification.application.query.GetNotificationQuery;
import com.architek.oikos.notification.domain.exception.NotificationNotFoundException;
import com.architek.oikos.notification.domain.repository.NotificationRepository;

@Component
public class GetNotificationService implements GetNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public GetNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationView getNotification(GetNotificationQuery query) {
        return notificationRepository.findById(query.id())
                .map(NotificationViewMapper::toView)
                .orElseThrow(() -> new NotificationNotFoundException(query.id()));
    }
}
