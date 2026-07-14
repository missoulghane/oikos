package com.architek.oikos.property.infrastructure.persistence;

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
@Table(name = "unit_ownership")
@Getter
@Setter
@NoArgsConstructor
public class UnitOwnershipEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "contact_id", nullable = false)
    private UUID contactId;

    @Column(name = "ownership_share", nullable = false, precision = 5, scale = 2)
    private BigDecimal ownershipShare;
}
