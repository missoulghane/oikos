package com.architek.oikos.notification.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.notification.application.command.CreateNotificationCommand;
import com.architek.oikos.notification.application.port.in.CreateNotificationUseCase;
import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.notification.domain.valueobject.NotificationId;

@Component
public class CreateNotificationService implements CreateNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public CreateNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public NotificationId create(CreateNotificationCommand command) {
        Notification created = Notification.create(NotificationId.newId(), command.recipientUserId(), command.propertyId(),
                command.type(), command.title(), command.body(), command.linkPath());
        return notificationRepository.save(created).getId();
    }
}
