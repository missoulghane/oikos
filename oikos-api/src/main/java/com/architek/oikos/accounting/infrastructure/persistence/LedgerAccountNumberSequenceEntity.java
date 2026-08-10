package com.architek.oikos.accounting.infrastructure.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * No audit columns (created_date/last_modified_date/version) - this table
 * is a pure counter, not a business entity (V11).
 */
@Entity
@Table(name = "ledger_account_number_sequence")
@IdClass(LedgerAccountNumberSequenceEntity.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class LedgerAccountNumberSequenceEntity {

    @Id
    @Column(name = "property_id")
    private UUID propertyId;

    @Id
    @Column(name = "number_prefix")
    private String numberPrefix;

    @Column(name = "next_increment", nullable = false)
    private int nextIncrement;

    public LedgerAccountNumberSequenceEntity(UUID propertyId, String numberPrefix, int nextIncrement) {
        this.propertyId = propertyId;
        this.numberPrefix = numberPrefix;
        this.nextIncrement = nextIncrement;
    }

    public static class Key implements Serializable {
        private UUID propertyId;
        private String numberPrefix;

        public Key() {
        }

        public Key(UUID propertyId, String numberPrefix) {
            this.propertyId = propertyId;
            this.numberPrefix = numberPrefix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof Key other && propertyId.equals(other.propertyId)
                    && numberPrefix.equals(other.numberPrefix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(propertyId, numberPrefix);
        }
    }
}
