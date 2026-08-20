package com.architek.oikos.property.infrastructure.persistence;

import java.math.BigDecimal;
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
import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "property")
@Getter
@Setter
@NoArgsConstructor
public class PropertyEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    /** Facultative : les coproprietes anterieures a ce champ n'en ont pas. */
    @Column(length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "dues_calculation_mode", nullable = false)
    private DuesCalculationMode duesCalculationMode;

    @Column(name = "projected_budget", precision = 12, scale = 2)
    private BigDecimal projectedBudget;
}
