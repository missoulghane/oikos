package com.architek.oikos.messaging.application.command;

import java.util.Set;

import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Nom et membres ensemble : l'écran de gestion enregistre le groupe entier. */
public record UpdateRecipientGroupCommand(RecipientGroupId groupId, EntityId propertyId, String name,
                                            Set<EntityId> memberUserIds) {
}
