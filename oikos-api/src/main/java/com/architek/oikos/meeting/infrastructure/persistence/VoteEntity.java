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

import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * No weight column: a vote's weight is its lot's convocation snapshot, and
 * storing it twice would only create a chance for the two to disagree (see
 * Vote's javadoc).
 */
@Entity
@Table(name = "vote")
@Getter
@Setter
@NoArgsConstructor
public class VoteEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "agenda_item_id", nullable = false)
    private UUID agendaItemId;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "choice", nullable = false)
    private VoteChoice choice;

    @Column(name = "cast_at", nullable = false)
    private Instant castAt;

    @Column(name = "cast_by_user_id")
    private UUID castByUserId;
}
