package com.architek.oikos.invitation.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;

public record ListAvailableUnitsForInvitationQuery(String token, PageRequest pageRequest) {
}
