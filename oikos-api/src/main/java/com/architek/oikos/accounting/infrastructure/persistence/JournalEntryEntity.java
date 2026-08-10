package com.architek.oikos.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "journal_entry")
@Getter
@Setter
@NoArgsConstructor
public class JournalEntryEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "journal_code", nullable = false)
    private String journalCode;

    @Column(name = "treasury_account_id")
    private UUID treasuryAccountId;

    @Column(name = "piece_date", nullable = false)
    private LocalDate pieceDate;

    @Column(name = "piece_number")
    private Integer pieceNumber;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(nullable = false)
    private String status;

    @Column(name = "original_entry_id")
    private UUID originalEntryId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    /**
     * Lines are only ever written once, at first save (I4: never touched
     * again once the entry leaves DRAFT) - the adapter never re-populates
     * this collection on a subsequent status-only update, so Hibernate never
     * re-issues insert/delete for already-persisted lines.
     */
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderColumn(name = "line_order")
    private List<JournalEntryLineEntity> lines = new ArrayList<>();
}
