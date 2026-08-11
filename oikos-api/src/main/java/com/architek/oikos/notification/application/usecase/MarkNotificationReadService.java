package com.architek.oikos.notification.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.notification.application.command.MarkNotificationReadCommand;
import com.architek.oikos.notification.application.port.in.MarkNotificationReadUseCase;
import com.architek.oikos.notification.domain.exception.NotificationNotFoundException;
import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.repository.NotificationRepository;

@Component
public class MarkNotificationReadService implements MarkNotificationReadUseCase {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public MarkNotificationReadService(NotificationRepository notificationRepository, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void markRead(MarkNotificationReadCommand command) {
        Notification notification = notificationRepository.findById(command.id())
                .orElseThrow(() -> new NotificationNotFoundException(command.id()));
        notificationRepository.save(notification.markRead(clock.instant()));
    }
}
