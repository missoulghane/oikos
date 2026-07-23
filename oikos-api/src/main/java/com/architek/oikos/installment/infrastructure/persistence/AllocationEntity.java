package com.architek.oikos.installment.infrastructure.persistence;

import java.math.BigDecimal;
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
@Table(name = "allocation")
@Getter
@Setter
@NoArgsConstructor
public class AllocationEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "movement_id", nullable = false)
    private UUID movementId;

    @Column(name = "installment_id", nullable = false)
    private UUID installmentId;

    @Column(name = "allocated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal allocatedAmount;
}
