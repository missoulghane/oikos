package com.architek.oikos.meeting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.VenueType;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * The venue is flattened into three columns rather than mapped as an
 * @Embeddable: two of them are nullable together and the third decides which,
 * which the MeetingVenue value object expresses far better than any embedded
 * mapping would - it is rebuilt in the persistence mapper.
 */
@Entity
@Table(name = "general_meeting")
@Getter
@Setter
@NoArgsConstructor
public class GeneralMeetingEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false)
    private MeetingType meetingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "venue_type")
    private VenueType venueType;

    @Column(name = "venue_address")
    private String venueAddress;

    @Column(name = "venue_link")
    private String venueLink;

    @Column(name = "quorum_percentage", nullable = false)
    private BigDecimal quorumPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "voting_weight_mode", nullable = false)
    private VotingWeightMode votingWeightMode;

    /** Rich-text HTML, hence `text` and not a bounded column - see V9 for why. */
    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "opened_without_quorum", nullable = false)
    private boolean openedWithoutQuorum;
}
