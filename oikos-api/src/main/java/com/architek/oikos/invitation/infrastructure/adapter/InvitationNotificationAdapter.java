package com.architek.oikos.invitation.infrastructure.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
 *
 * <p>REQUIRES_NEW, et non le REQUIRED par défaut, pour les deux raisons que
 * documente MembershipDecisionNotificationListener : appelées en phase
 * after-commit, ces écritures rejoindraient sinon une transaction en cours
 * d'achèvement et seraient jetées en silence ; et une notification qui échoue
 * ne doit pas condamner celles des autres destinataires, chacune ayant sa
 * propre transaction plutôt qu'une transaction partagée qu'un seul échec suffit
 * à marquer rollback-only.
 */
@Component
public class InvitationNotificationAdapter implements NotificationPort {

    private final CreateNotificationUseCase createNotificationUseCase;

    public InvitationNotificationAdapter(CreateNotificationUseCase createNotificationUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyRequestReceived(EntityId recipientUserId, EntityId propertyId, String title, String body, String linkPath) {
        createNotificationUseCase.create(new CreateNotificationCommand(recipientUserId, propertyId,
                NotificationType.REQUEST_RECEIVED, title, body, linkPath));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyRequestDecided(EntityId recipientUserId, EntityId propertyId, String title, String body, String linkPath) {
        createNotificationUseCase.create(new CreateNotificationCommand(recipientUserId, propertyId,
                NotificationType.REQUEST_DECIDED, title, body, linkPath));
    }
}
