package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
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
@Table(name = "unit_account_allocation")
@Getter
@Setter
@NoArgsConstructor
public class UnitAccountAllocationEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "unit_account_id", nullable = false)
    private UUID unitAccountId;

    @Column(name = "debit_movement_id", nullable = false)
    private UUID debitMovementId;

    @Column(name = "credit_movement_id", nullable = false)
    private UUID creditMovementId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "allocated_date", nullable = false)
    private LocalDate allocatedDate;

    @Column(name = "allocated_by_user_id", nullable = false)
    private UUID allocatedByUserId;
}
