package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "journal_entry_line")
@Getter
@Setter
@NoArgsConstructor
public class JournalEntryLineEntity extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntryEntity journalEntry;

    @Column(name = "ledger_account_id", nullable = false)
    private UUID ledgerAccountId;

    @Column(name = "auxiliary_unit_id")
    private UUID auxiliaryUnitId;

    @Column(name = "auxiliary_party_id")
    private UUID auxiliaryPartyId;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String label;

    /** Preserves the line's position within its JournalEntry (spec §4.1 display order) -
     * see JournalEntryEntity.lines: with mappedBy, Hibernate can't manage this via
     * @OrderColumn (HHH160246), so it's a real mapped column, set explicitly by the mapper. */
    @Column(name = "line_order", nullable = false)
    private int lineOrder;
}
