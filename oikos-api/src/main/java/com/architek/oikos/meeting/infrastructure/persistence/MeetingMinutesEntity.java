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

import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "meeting_minutes")
@Getter
@Setter
@NoArgsConstructor
public class MeetingMinutesEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "general_meeting_id", nullable = false)
    private UUID generalMeetingId;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MinutesStatus status;

    @Column(name = "published_at")
    private Instant publishedAt;
}
