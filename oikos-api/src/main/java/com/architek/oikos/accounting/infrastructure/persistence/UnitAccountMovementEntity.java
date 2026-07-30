package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
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
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "unit_account_movement")
@Getter
@Setter
@NoArgsConstructor
public class UnitAccountMovementEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "unit_account_id", nullable = false)
    private UUID unitAccountId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitAccountMovementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitAccountMovementDirection direction;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "business_reference")
    private String businessReference;

    @Column(nullable = false)
    private String label;

    @Column
    private String reason;
}
