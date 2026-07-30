package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "financial_journal_entry")
@Getter
@Setter
@NoArgsConstructor
public class FinancialJournalEntryEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "financial_account_id", nullable = false)
    private UUID financialAccountId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialEntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialEntryDirection direction;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String label;

    @Column(name = "business_reference")
    private String businessReference;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;
}
