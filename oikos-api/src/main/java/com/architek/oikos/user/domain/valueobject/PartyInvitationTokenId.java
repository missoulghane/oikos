package com.architek.oikos.user.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PartyInvitationTokenId(EntityId value) {

    public PartyInvitationTokenId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PartyInvitationTokenId newId() {
        return new PartyInvitationTokenId(EntityId.newId());
    }

    public static PartyInvitationTokenId of(UUID value) {
        return new PartyInvitationTokenId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
