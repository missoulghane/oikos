package com.architek.oikos.user.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record OnboardingLeadId(EntityId value) {

    public OnboardingLeadId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static OnboardingLeadId newId() {
        return new OnboardingLeadId(EntityId.newId());
    }

    public static OnboardingLeadId of(UUID value) {
        return new OnboardingLeadId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
