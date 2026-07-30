package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "expense")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "financial_account_id", nullable = false)
    private UUID financialAccountId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column
    private String description;

    @Column(name = "receipt_reference")
    private String receiptReference;

    @Column(name = "journal_entry_id", nullable = false)
    private UUID journalEntryId;
}
