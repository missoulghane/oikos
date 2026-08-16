package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MeetingQuorumSettingId(EntityId value) {

    public MeetingQuorumSettingId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MeetingQuorumSettingId newId() {
        return new MeetingQuorumSettingId(EntityId.newId());
    }

    public static MeetingQuorumSettingId of(UUID value) {
        return new MeetingQuorumSettingId(EntityId.of(value));
    }

    public static MeetingQuorumSettingId of(String value) {
        return new MeetingQuorumSettingId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
