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

/** I7: continuous, gap-free piece numbering per (property, exercise, journal). No audit columns - a pure counter. */
@Entity
@Table(name = "sequence_piece")
@IdClass(SequencePieceEntity.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class SequencePieceEntity {

    @Id
    @Column(name = "property_id")
    private UUID propertyId;

    @Id
    @Column(name = "exercise_id")
    private UUID exerciseId;

    @Id
    @Column(name = "journal_code")
    private String journalCode;

    @Column(name = "next_number", nullable = false)
    private int nextNumber;

    public SequencePieceEntity(UUID propertyId, UUID exerciseId, String journalCode, int nextNumber) {
        this.propertyId = propertyId;
        this.exerciseId = exerciseId;
        this.journalCode = journalCode;
        this.nextNumber = nextNumber;
    }

    public static class Key implements Serializable {
        private UUID propertyId;
        private UUID exerciseId;
        private String journalCode;

        public Key() {
        }

        public Key(UUID propertyId, UUID exerciseId, String journalCode) {
            this.propertyId = propertyId;
            this.exerciseId = exerciseId;
            this.journalCode = journalCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof Key other && propertyId.equals(other.propertyId)
                    && exerciseId.equals(other.exerciseId) && journalCode.equals(other.journalCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(propertyId, exerciseId, journalCode);
        }
    }
}
