package com.architek.oikos.messaging.application.command;

import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Un envoi groupé porte son propre objet : c'est un message, pas une reprise du canal d'annonces. */
public record SendBroadcastMessageCommand(EntityId propertyId, EntityId senderId, ConversationSubject subject,
                                           MessageBody body) {
}
