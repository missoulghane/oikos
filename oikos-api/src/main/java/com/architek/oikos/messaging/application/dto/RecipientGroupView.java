package com.architek.oikos.messaging.application.dto;

import java.util.List;

import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Un groupe tel qu'il s'affiche : ses membres portent déjà leur nom, pour que
 * l'écran de gestion et le sélecteur de destinataires n'aient pas à recroiser
 * l'annuaire eux-mêmes.
 */
public record RecipientGroupView(RecipientGroupId id, EntityId propertyId, String name,
                                  List<ConversationParticipantView> members) {
}
