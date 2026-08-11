package com.architek.oikos.messaging.application.dto;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RecipientCandidateView(EntityId userId, String fullName, String roleLabel, List<String> unitNumbers,
                                      boolean isStaff) {
}
