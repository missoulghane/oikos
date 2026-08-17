package com.architek.oikos.meeting.infrastructure.persistence;

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
 * One answer given for a lot. convocationId is a plain UUID column backed by a
 * real foreign key rather than a @ManyToOne, for the same reason as
 * {@link ConvocationDeliveryEntity}: the aggregate is loaded and saved whole
 * through its own adapter, and an association here would give the child a
 * second way to reach - and modify - its parent.
 *
 * <p>receivedAt is declared and backdatable; createdDate comes from
 * AuditableEntity and is when the row was written. Both are needed to order the
 * history - see ConvocationReply.LATEST_FIRST.
 */
@Entity
@Table(name = "convocation_reply")
@Getter
@Setter
@NoArgsConstructor
public class ConvocationReplyEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "convocation_id", nullable = false)
    private UUID convocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_reply", nullable = false)
    private AttendanceReply attendanceReply;

    @Enumerated(EnumType.STRING)
    @Column(name = "reply_source", nullable = false)
    private ReplySource replySource;

    @Column(name = "reply_medium")
    private String replyMedium;

    /** How the lot announced it would attend - null unless the answer is ATTENDING. */
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_mode")
    private AttendanceMode attendanceMode;

    /** A stand-in was announced. Not the mandate itself - see ADR 0002 §7. */
    @Column(name = "by_proxy", nullable = false)
    private boolean byProxy;

    @Column(name = "replied_by_party_id")
    private UUID repliedByPartyId;

    @Column(name = "note")
    private String note;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "recorded_by_user_id")
    private UUID recordedByUserId;
}
