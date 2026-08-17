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

import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * One attempt at delivering a convocation. convocationId is a plain UUID column
 * backed by a real foreign key rather than a @ManyToOne: the aggregate is
 * loaded and saved as a whole through its own adapter, and an association here
 * would give the child a second way to reach - and modify - its parent.
 */
@Entity
@Table(name = "convocation_delivery")
@Getter
@Setter
@NoArgsConstructor
public class ConvocationDeliveryEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "convocation_id", nullable = false)
    private UUID convocationId;

    @Column(name = "channel_code", nullable = false)
    private String channelCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "reference")
    private String reference;

    /** A chase rather than the convocation itself - display only, nothing computes on it. */
    @Column(name = "is_reminder", nullable = false)
    private boolean reminder;

    @Column(name = "recorded_by_user_id")
    private UUID recordedByUserId;
}
