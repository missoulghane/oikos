package com.architek.oikos.invitation.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MembershipRequestId(EntityId value) {

    public MembershipRequestId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MembershipRequestId newId() {
        return new MembershipRequestId(EntityId.newId());
    }

    public static MembershipRequestId of(UUID value) {
        return new MembershipRequestId(EntityId.of(value));
    }

    public static MembershipRequestId of(String value) {
        return new MembershipRequestId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
