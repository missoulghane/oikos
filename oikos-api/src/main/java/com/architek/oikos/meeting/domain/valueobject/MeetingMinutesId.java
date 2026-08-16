package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MeetingMinutesId(EntityId value) {

    public MeetingMinutesId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MeetingMinutesId newId() {
        return new MeetingMinutesId(EntityId.newId());
    }

    public static MeetingMinutesId of(UUID value) {
        return new MeetingMinutesId(EntityId.of(value));
    }

    public static MeetingMinutesId of(String value) {
        return new MeetingMinutesId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
