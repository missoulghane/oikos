package com.architek.oikos.property.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The unit type catalog. Not an AuditableEntity: reference rows are seeded by migration. */
@Entity
@Table(name = "unit_type_choice")
@Getter
@Setter
@NoArgsConstructor
public class UnitTypeChoiceEntity {

    @Id
    private String name;

    @Column(name = "plural", nullable = false)
    private String plural;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "active", nullable = false)
    private boolean active;
}
