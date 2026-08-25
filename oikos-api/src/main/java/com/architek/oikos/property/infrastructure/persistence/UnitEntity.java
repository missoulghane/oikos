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
@Table(name = "unit")
@Getter
@Setter
@NoArgsConstructor
public class UnitEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "unit_number", nullable = false)
    private String unitNumber;

    @Column(name = "unit_type_id", nullable = false)
    private UUID unitTypeId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shares;

    /** Nullable : l'etage n'est pas connu pour les lots crees avant V9, ni pour ceux generes en masse. */
    @Column(name = "floor")
    private Integer floor;
}
