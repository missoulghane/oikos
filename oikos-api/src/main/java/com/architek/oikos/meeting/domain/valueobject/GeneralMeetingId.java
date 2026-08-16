package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record GeneralMeetingId(EntityId value) {

    public GeneralMeetingId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static GeneralMeetingId newId() {
        return new GeneralMeetingId(EntityId.newId());
    }

    public static GeneralMeetingId of(UUID value) {
        return new GeneralMeetingId(EntityId.of(value));
    }

    public static GeneralMeetingId of(String value) {
        return new GeneralMeetingId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
