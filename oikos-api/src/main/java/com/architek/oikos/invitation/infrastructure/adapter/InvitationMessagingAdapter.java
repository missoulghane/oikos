package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.port.out.MessagingPort;
import com.architek.oikos.messaging.application.command.StartGroupConversationCommand;
import com.architek.oikos.messaging.application.port.in.StartGroupConversationUseCase;
import com.architek.oikos.messaging.domain.exception.RecipientNotPropertyMemberException;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to messaging's public port-in
 * (StartGroupConversationUseCase), never to its repositories directly
 * (rule 6).
 *
 * <p>RecipientNotPropertyMemberException est rattrapée ici plutôt que
 * propagée : c'est le cas nominal d'un refus (le demandeur n'a jamais reçu de
 * rôle sur la copropriété, il n'est donc pas un destinataire légitime de la
 * messagerie), et une décision de syndic ne doit pas échouer parce que l'un de
 * ses quatre canaux d'annonce n'était pas ouvert. Les autres partent quand même.
 *
 * <p>REQUIRES_NEW pour la même raison que InvitationNotificationAdapter : appelé
 * en phase after-commit, et isolé des autres canaux, qu'un refus de messaging ne
 * doit pas entraîner dans son rollback.
 */
@Component
public class InvitationMessagingAdapter implements MessagingPort {

    private static final Logger log = LoggerFactory.getLogger(InvitationMessagingAdapter.class);

    private final StartGroupConversationUseCase startGroupConversationUseCase;

    public InvitationMessagingAdapter(StartGroupConversationUseCase startGroupConversationUseCase) {
        this.startGroupConversationUseCase = startGroupConversationUseCase;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean sendDecisionMessage(EntityId propertyId, EntityId senderUserId, EntityId recipientUserId,
                                        String subject, String body) {
        try {
            startGroupConversationUseCase.start(new StartGroupConversationCommand(propertyId, senderUserId,
                    Set.of(recipientUserId), ConversationSubject.of(subject), MessageBody.of(body),
                    SenderIdentity.BOARD, null));
            return true;
        } catch (RecipientNotPropertyMemberException e) {
            log.info("Decision message not delivered to user {} on property {}: {}", recipientUserId, propertyId,
                    e.getMessage());
            return false;
        }
    }
}
