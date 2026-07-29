package com.architek.oikos.party.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * Mirrors the uk_party_property_email / uk_party_property_phone constraints from
 * V1__baseline.sql, so schema-from-entity environments (dev H2, tests) enforce the
 * same per-property uniqueness as the real Flyway-managed schema. The phone
 * constraint only works because Party normalizes blank phone to null (SQL NULL is
 * never equal to itself, matching the migration's "WHERE phone IS NOT NULL" partial index).
 */
@Entity
@Table(name = "party", uniqueConstraints = {
        @UniqueConstraint(name = "uk_party_property_email", columnNames = {"property_id", "email"}),
        @UniqueConstraint(name = "uk_party_property_phone", columnNames = {"property_id", "phone"})
})
@Getter
@Setter
@NoArgsConstructor
public class PartyEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Column(nullable = false)
    private String email;

    @Column
    private String phone;
}
