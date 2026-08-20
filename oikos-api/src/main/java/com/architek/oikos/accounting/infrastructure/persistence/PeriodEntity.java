package com.architek.oikos.accounting.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "period")
@Getter
@Setter
@NoArgsConstructor
public class PeriodEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "year_month", nullable = false)
    private LocalDate yearMonth;

    @Column(nullable = false)
    private String status;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by_user_id")
    private UUID closedByUserId;

    /** Trace de la dernière réouverture : le statut ne la garderait pas. */
    @Column(name = "reopened_at")
    private Instant reopenedAt;

    @Column(name = "reopened_by_user_id")
    private UUID reopenedByUserId;
}
