package com.architek.oikos.accounting.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
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
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "accounting_exercise")
@Getter
@Setter
@NoArgsConstructor
public class AccountingExerciseEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(nullable = false)
    private String label;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExerciseStatus status;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by_user_id")
    private UUID closedByUserId;

    @Column
    private String comment;
}
