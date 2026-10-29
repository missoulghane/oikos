package com.architek.oikos.property.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The board seat catalog. Not an AuditableEntity: reference rows are seeded by migration. */
@Entity
@Table(name = "board_role")
@Getter
@Setter
@NoArgsConstructor
public class BoardRoleOptionEntity {

    @Id
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "available_at_signup", nullable = false)
    private boolean availableAtSignup;

    @Column(name = "active", nullable = false)
    private boolean active;
}
