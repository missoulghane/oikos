package com.architek.oikos.installment.infrastructure.persistence;

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

/** Continuous receipt numbering per (property, year). No audit columns - a pure counter, like SequencePieceEntity. */
@Entity
@Table(name = "sequence_receipt")
@IdClass(SequenceReceiptEntity.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class SequenceReceiptEntity {

    @Id
    @Column(name = "property_id")
    private UUID propertyId;

    @Id
    // See V3: `year` is reserved in H2, hence the column name.
    @Column(name = "receipt_year")
    private int year;

    @Column(name = "next_number", nullable = false)
    private int nextNumber;

    public SequenceReceiptEntity(UUID propertyId, int year, int nextNumber) {
        this.propertyId = propertyId;
        this.year = year;
        this.nextNumber = nextNumber;
    }

    public static class Key implements Serializable {
        private UUID propertyId;
        private int year;

        public Key() {
        }

        public Key(UUID propertyId, int year) {
            this.propertyId = propertyId;
            this.year = year;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof Key other && propertyId.equals(other.propertyId) && year == other.year;
        }

        @Override
        public int hashCode() {
            return Objects.hash(propertyId, year);
        }
    }
}
