package com.architek.oikos.messaging.application.dto;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RecipientCandidateView(EntityId userId, String fullName, String roleLabel) {
}
