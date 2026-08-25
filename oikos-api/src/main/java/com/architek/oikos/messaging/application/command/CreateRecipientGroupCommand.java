package com.architek.oikos.messaging.application.command;

import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CreateRecipientGroupCommand(EntityId propertyId, EntityId createdBy, String name,
                                            Set<EntityId> memberUserIds) {
}
