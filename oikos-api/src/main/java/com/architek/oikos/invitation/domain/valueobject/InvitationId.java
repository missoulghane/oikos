package com.architek.oikos.invitation.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InvitationId(EntityId value) {

    public InvitationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static InvitationId newId() {
        return new InvitationId(EntityId.newId());
    }

    public static InvitationId of(UUID value) {
        return new InvitationId(EntityId.of(value));
    }

    public static InvitationId of(String value) {
        return new InvitationId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
