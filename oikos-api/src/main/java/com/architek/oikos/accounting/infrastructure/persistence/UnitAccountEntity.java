package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
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
@Table(name = "unit_account")
@Getter
@Setter
@NoArgsConstructor
public class UnitAccountEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "unit_id", nullable = false, unique = true)
    private UUID unitId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(name = "last_updated_date", nullable = false)
    private Instant lastUpdatedDate;
}
