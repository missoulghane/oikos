package com.architek.oikos.meeting.infrastructure.persistence;

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

import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * uk_agenda_item_position (general_meeting_id, position) is deliberately NOT
 * declared here. The constraint exists in PostgreSQL as DEFERRABLE INITIALLY
 * DEFERRED so a reordering can pass through duplicate positions inside a
 * transaction; declaring it on the entity would have Hibernate recreate it as
 * a plain immediate constraint on H2 (the dev and test engine, ddl-auto), where
 * every reordering would then fail. Hibernate's schema validation does not
 * check unique constraints, so the docker profile is unaffected.
 */
@Entity
@Table(name = "agenda_item")
@Getter
@Setter
@NoArgsConstructor
public class AgendaItemEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "general_meeting_id", nullable = false)
    private UUID generalMeetingId;

    @Column(nullable = false)
    private String label;

    @Column
    private String description;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "majority_rule", nullable = false)
    private MajorityRule majorityRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_session_status", nullable = false)
    private VoteSessionStatus voteSessionStatus;
}
