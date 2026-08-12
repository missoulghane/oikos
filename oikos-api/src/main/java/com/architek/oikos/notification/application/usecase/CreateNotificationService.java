package com.architek.oikos.notification.application.usecase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.notification.application.command.CreateNotificationCommand;
import com.architek.oikos.notification.application.port.in.CreateNotificationUseCase;
import com.architek.oikos.notification.application.port.out.PushNotificationPort;
import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.repository.DevicePushTokenRepository;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.notification.domain.valueobject.NotificationId;

// Plain SLF4J logger, not Lombok's @Slf4j: domain/application source files
// must not import lombok (see LombokUsageSourceTest) - lombok is reserved
// for infrastructure/web layers in this codebase.
@Component
public class CreateNotificationService implements CreateNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateNotificationService.class);

    private final NotificationRepository notificationRepository;
    private final DevicePushTokenRepository devicePushTokenRepository;
    private final PushNotificationPort pushNotificationPort;

    public CreateNotificationService(NotificationRepository notificationRepository,
                                      DevicePushTokenRepository devicePushTokenRepository,
                                      PushNotificationPort pushNotificationPort) {
        this.notificationRepository = notificationRepository;
        this.devicePushTokenRepository = devicePushTokenRepository;
        this.pushNotificationPort = pushNotificationPort;
    }

    @Override
    @Transactional
    public NotificationId create(CreateNotificationCommand command) {
        Notification created = Notification.create(NotificationId.newId(), command.recipientUserId(), command.propertyId(),
                command.type(), command.title(), command.body(), command.linkPath());
        NotificationId savedId = notificationRepository.save(created).getId();
        sendPushBestEffort(command);
        return savedId;
    }

    // A push delivery failure (or simply no registered device) must never
    // roll back the Notification row itself - the in-app inbox is the
    // source of truth, push is a best-effort nudge on top of it.
    private void sendPushBestEffort(CreateNotificationCommand command) {
        try {
            List<String> tokens = devicePushTokenRepository.findTokensByUserId(command.recipientUserId());
            if (!tokens.isEmpty()) {
                pushNotificationPort.sendPush(tokens, command.title(), command.body(), command.linkPath());
            }
        } catch (Exception e) {
            log.warn("Failed to send push notification for recipient {}", command.recipientUserId(), e);
        }
    }
}
