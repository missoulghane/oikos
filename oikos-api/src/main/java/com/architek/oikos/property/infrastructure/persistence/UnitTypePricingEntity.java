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
@Table(name = "unit_type_pricing")
@Getter
@Setter
@NoArgsConstructor
public class UnitTypePricingEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "unit_type_id", nullable = false)
    private UUID unitTypeId;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}
