package com.architek.oikos.meeting.domain.model;

import java.util.Objects;

import com.architek.oikos.meeting.domain.valueobject.MeetingQuorumSettingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The quorum a property requires for one kind of meeting - ordinary and
 * extraordinary do not answer to the same threshold, and the threshold itself
 * varies with the règlement de copropriété, so it is configuration and not a
 * constant.
 *
 * <p>One row per (property, meetingType); no row means no quorum required
 * (see QuorumPercentage.none, and why no legal default is invented). The
 * value is copied onto the meeting at creation, so editing it here never
 * restates a meeting already held.
 *
 * <p>Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class MeetingQuorumSetting {

    private final MeetingQuorumSettingId id;
    private final EntityId propertyId;
    private final MeetingType meetingType;
    private final QuorumPercentage quorumPercentage;

    private MeetingQuorumSetting(MeetingQuorumSettingId id, EntityId propertyId, MeetingType meetingType,
                                  QuorumPercentage quorumPercentage) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.meetingType = Objects.requireNonNull(meetingType, "meetingType must not be null");
        this.quorumPercentage = Objects.requireNonNull(quorumPercentage, "quorumPercentage must not be null");
    }

    public static MeetingQuorumSetting create(MeetingQuorumSettingId id, EntityId propertyId, MeetingType meetingType,
                                               QuorumPercentage quorumPercentage) {
        return new MeetingQuorumSetting(id, propertyId, meetingType, quorumPercentage);
    }

    public static MeetingQuorumSetting reconstruct(MeetingQuorumSettingId id, EntityId propertyId,
                                                    MeetingType meetingType, QuorumPercentage quorumPercentage) {
        return new MeetingQuorumSetting(id, propertyId, meetingType, quorumPercentage);
    }

    public MeetingQuorumSetting withQuorumPercentage(QuorumPercentage newQuorumPercentage) {
        return new MeetingQuorumSetting(id, propertyId, meetingType, newQuorumPercentage);
    }

    public MeetingQuorumSettingId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public MeetingType getMeetingType() {
        return meetingType;
    }

    public QuorumPercentage getQuorumPercentage() {
        return quorumPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof MeetingQuorumSetting other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
