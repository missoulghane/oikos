package com.architek.oikos.invitation.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.NotificationPort;
import com.architek.oikos.notification.application.command.CreateNotificationCommand;
import com.architek.oikos.notification.application.port.in.CreateNotificationUseCase;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates exclusively to notification's public
 * port-in (CreateNotificationUseCase), never to its repository directly
 * (rule 6). First real caller of CreateNotificationUseCase (see its javadoc:
 * "not called by anything yet").
 */
@Component
public class InvitationNotificationAdapter implements NotificationPort {

    private final CreateNotificationUseCase createNotificationUseCase;

    public InvitationNotificationAdapter(CreateNotificationUseCase createNotificationUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
    }

    @Override
    public void notifyRequestReceived(EntityId recipientUserId, EntityId propertyId, String title, String body, String linkPath) {
        createNotificationUseCase.create(new CreateNotificationCommand(recipientUserId, propertyId,
                NotificationType.REQUEST_RECEIVED, title, body, linkPath));
    }
}
