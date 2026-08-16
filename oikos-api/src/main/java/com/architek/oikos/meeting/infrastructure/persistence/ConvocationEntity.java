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

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * unitId, checkedInPartyId and repliedByPartyId are plain UUID columns backed
 * by real foreign keys, but typed here as UUID rather than as property's/party's
 * own id types: this module never depends on their identity classes (rule 4).
 *
 * <p>No channel, sentAt or deliveryStatus column: sending is a list of
 * convocation_delivery rows since V6, and the two summary values the screens
 * still need are derived by the aggregate from that list.
 */
@Entity
@Table(name = "convocation")
@Getter
@Setter
@NoArgsConstructor
public class ConvocationEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "general_meeting_id", nullable = false)
    private UUID generalMeetingId;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "voting_weight", nullable = false)
    private BigDecimal votingWeight;

    /** Unique in V8: the token is the only thing an anonymous visitor presents, so two
     * convocations sharing one would be an answer recorded on the wrong lot. */
    @Column(name = "confirmation_token", nullable = false, unique = true, length = 64)
    private String confirmationToken;

    /** Unique per meeting, not globally - the meeting's public reference is always presented
     * with it, and per-meeting scoping is what bounds a guess to one copropriété's lots. */
    @Column(name = "confirmation_code", nullable = false, length = 6)
    private String confirmationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_reply", nullable = false)
    private AttendanceReply attendanceReply;

    @Column(name = "replied_at")
    private Instant repliedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reply_source")
    private ReplySource replySource;

    @Column(name = "replied_by_party_id")
    private UUID repliedByPartyId;

    @Column(name = "reply_note")
    private String replyNote;

    @Column(name = "checked_in", nullable = false)
    private boolean checkedIn;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_mode")
    private AttendanceMode attendanceMode;

    @Column(name = "checked_in_party_id")
    private UUID checkedInPartyId;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;
}
