package com.architek.oikos.auth.application.dto;

import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AuthenticatedPrincipal(EntityId userId, Set<String> authorities) {
}
