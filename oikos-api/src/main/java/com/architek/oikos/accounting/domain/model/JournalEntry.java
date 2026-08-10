package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.ArrayList;

import com.architek.oikos.accounting.domain.exception.InsufficientJournalEntryLinesException;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotDraftException;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotPostedException;
import com.architek.oikos.accounting.domain.exception.NonPostableJournalException;
import com.architek.oikos.accounting.domain.exception.TreasuryJournalLineMismatchException;
import com.architek.oikos.accounting.domain.exception.UnbalancedJournalEntryException;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.JournalType;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A double-entry accounting entry (spec &sect;4.1/&sect;5) - the aggregate
 * root of the PCM engine, replacing the pre-PCM FinancialJournalEntry
 * (single account, single direction). Lines are supplied whole at
 * construction time (draft()) by the use case that built them, not added
 * incrementally: I2 (minimum 2 lines) is checked immediately, I1 (debits =
 * credits) only at post() time (spec table: "a la validation"), so a DRAFT
 * entry may be transiently unbalanced while edited (PATCH /ecritures/{id}
 * in the API surface, a later phase).
 *
 * <p>I4 (immutability once posted) follows from this class never exposing a
 * mutator - post()/markReversed() each return a new instance - backed by the
 * DB triggers added in V12 as defense in depth. I5 (piece date must fall in
 * an OPEN period of an OPEN exercise) needs sibling aggregates
 * (Period/AccountingExercise) and is therefore an application-layer
 * precondition, not checked here. Contre-passation (P10) is
 * mirrorLinesForReversal() + markReversed(); building and posting the
 * actual mirror JournalEntry (a fresh id, period, piece number) is the
 * responsibility of a later phase's use case.
 */
public final class JournalEntry {

    private final JournalEntryId id;
    private final EntityId propertyId;
    private final AccountingExerciseId exerciseId;
    private final PeriodId periodId;
    private final JournalCode journalCode;
    private final LedgerAccountId treasuryAccountId;
    private final LocalDate pieceDate;
    private final Integer pieceNumber;
    private final String externalReference;
    private final JournalEntryStatus status;
    private final JournalEntryId originalEntryId;
    private final EntityId createdByUserId;
    private final List<JournalEntryLine> lines;

    private JournalEntry(JournalEntryId id, EntityId propertyId, AccountingExerciseId exerciseId, PeriodId periodId,
                          JournalCode journalCode, LedgerAccountId treasuryAccountId, LocalDate pieceDate,
                          Integer pieceNumber, String externalReference, JournalEntryStatus status,
                          JournalEntryId originalEntryId, EntityId createdByUserId, List<JournalEntryLine> lines) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.exerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        this.periodId = Objects.requireNonNull(periodId, "periodId must not be null");
        this.journalCode = Objects.requireNonNull(journalCode, "journalCode must not be null");
        this.treasuryAccountId = treasuryAccountId;
        this.pieceDate = Objects.requireNonNull(pieceDate, "pieceDate must not be null");
        this.pieceNumber = pieceNumber;
        this.externalReference = externalReference;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.originalEntryId = originalEntryId;
        this.createdByUserId = Objects.requireNonNull(createdByUserId, "createdByUserId must not be null");
        this.lines = List.copyOf(Objects.requireNonNull(lines, "lines must not be null"));
    }

    /**
     * Creates a new DRAFT entry from a fully-built line list. Checks I2 and
     * the journal-level structural rules (spec &sect;3.3); I1 is checked at
     * post(), not here.
     */
    public static JournalEntry draft(JournalEntryId id, EntityId propertyId, AccountingExerciseId exerciseId,
                                      PeriodId periodId, JournalCode journalCode, LedgerAccountId treasuryAccountId,
                                      LocalDate pieceDate, String externalReference, EntityId createdByUserId,
                                      List<JournalEntryLine> lines) {
        return draft(id, propertyId, exerciseId, periodId, journalCode, treasuryAccountId, pieceDate,
                externalReference, createdByUserId, lines, null);
    }

    /**
     * Same as draft(...), with an originalEntryId - used only by the P10
     * contre-passation flow to build the mirror entry (see
     * mirrorLinesForReversal()). Goes through the exact same structural
     * checks (I2, treasury binding, postable journal) as any other entry.
     */
    public static JournalEntry draft(JournalEntryId id, EntityId propertyId, AccountingExerciseId exerciseId,
                                      PeriodId periodId, JournalCode journalCode, LedgerAccountId treasuryAccountId,
                                      LocalDate pieceDate, String externalReference, EntityId createdByUserId,
                                      List<JournalEntryLine> lines, JournalEntryId originalEntryId) {
        if (!journalCode.postable()) {
            throw new NonPostableJournalException(journalCode);
        }
        if (lines.size() < 2) {
            throw new InsufficientJournalEntryLinesException(lines.size());
        }
        boolean isTreasury = journalCode.type() == JournalType.TREASURY;
        if (isTreasury) {
            if (treasuryAccountId == null) {
                throw new IllegalArgumentException("a treasury journal entry (" + journalCode + ") requires a treasuryAccountId");
            }
            long matchingLines = lines.stream().filter(line -> line.getLedgerAccountId().equals(treasuryAccountId)).count();
            if (matchingLines != 1) {
                throw new TreasuryJournalLineMismatchException(journalCode, treasuryAccountId, matchingLines);
            }
        } else if (treasuryAccountId != null) {
            throw new IllegalArgumentException("a non-treasury journal entry (" + journalCode + ") must not carry a treasuryAccountId");
        }
        return new JournalEntry(id, propertyId, exerciseId, periodId, journalCode, treasuryAccountId, pieceDate,
                null, externalReference, JournalEntryStatus.DRAFT, originalEntryId, createdByUserId, lines);
    }

    public static JournalEntry reconstruct(JournalEntryId id, EntityId propertyId, AccountingExerciseId exerciseId,
                                            PeriodId periodId, JournalCode journalCode, LedgerAccountId treasuryAccountId,
                                            LocalDate pieceDate, Integer pieceNumber, String externalReference,
                                            JournalEntryStatus status, JournalEntryId originalEntryId,
                                            EntityId createdByUserId, List<JournalEntryLine> lines) {
        return new JournalEntry(id, propertyId, exerciseId, periodId, journalCode, treasuryAccountId, pieceDate,
                pieceNumber, externalReference, status, originalEntryId, createdByUserId, lines);
    }

    /**
     * I1: debits must equal credits, checked only now - a DRAFT entry may be
     * transiently unbalanced. I7 (continuous, gap-free numbering) is the
     * caller's responsibility (JournalEntryRepository.nextPieceNumber,
     * allocated under a row lock) - this only rejects an obviously invalid
     * value.
     */
    public JournalEntry post(int pieceNumber) {
        if (status != JournalEntryStatus.DRAFT) {
            throw new JournalEntryNotDraftException(id, status);
        }
        if (pieceNumber < 1) {
            throw new IllegalArgumentException("pieceNumber must be positive: " + pieceNumber);
        }
        BigDecimal totalDebit = sumOf(EntryDirection.DEBIT);
        BigDecimal totalCredit = sumOf(EntryDirection.CREDIT);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new UnbalancedJournalEntryException(id, totalDebit, totalCredit);
        }
        return new JournalEntry(id, propertyId, exerciseId, periodId, journalCode, treasuryAccountId, pieceDate,
                pieceNumber, externalReference, JournalEntryStatus.POSTED, originalEntryId, createdByUserId, lines);
    }

    /**
     * P10 (contre-passation): builds the mirror lines for a reversal of this
     * entry - same account/auxiliary/amount, flipped direction, label
     * prefixed. Pure: does not itself transition this entry to REVERSED
     * (see markReversed()) nor build the reversal JournalEntry (the caller
     * wraps the result in draft(..., originalEntryId=this.id)). newLineIds
     * must be supplied by the caller, one per line, in the same order.
     */
    public List<JournalEntryLine> mirrorLinesForReversal(List<JournalEntryLineId> newLineIds) {
        if (newLineIds.size() != lines.size()) {
            throw new IllegalArgumentException(
                    "newLineIds must have exactly " + lines.size() + " entries, got " + newLineIds.size());
        }
        List<JournalEntryLine> mirrored = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            JournalEntryLine original = lines.get(i);
            EntryDirection flipped = original.getDirection() == EntryDirection.DEBIT
                    ? EntryDirection.CREDIT
                    : EntryDirection.DEBIT;
            mirrored.add(JournalEntryLine.of(newLineIds.get(i), original.getLedgerAccountId(),
                    original.getAuxiliaryUnitId().orElse(null), original.getAuxiliaryPartyId().orElse(null),
                    flipped, original.getAmount(), "Extourne: " + original.getLabel()));
        }
        return mirrored;
    }

    /**
     * P10/I4: the original entry transitions to REVERSED once its mirror has
     * been posted - its lines are untouched (spec &sect;10: "ses lignes
     * restent intactes"), backed by the same DB trigger that enforces this
     * transition (V12).
     */
    public JournalEntry markReversed() {
        if (status != JournalEntryStatus.POSTED) {
            throw new JournalEntryNotPostedException(id, status);
        }
        return new JournalEntry(id, propertyId, exerciseId, periodId, journalCode, treasuryAccountId, pieceDate,
                pieceNumber, externalReference, JournalEntryStatus.REVERSED, originalEntryId, createdByUserId, lines);
    }

    private BigDecimal sumOf(EntryDirection direction) {
        return lines.stream()
                .filter(line -> line.getDirection() == direction)
                .map(line -> line.getAmount().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public JournalEntryId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public AccountingExerciseId getExerciseId() {
        return exerciseId;
    }

    public PeriodId getPeriodId() {
        return periodId;
    }

    public JournalCode getJournalCode() {
        return journalCode;
    }

    public Optional<LedgerAccountId> getTreasuryAccountId() {
        return Optional.ofNullable(treasuryAccountId);
    }

    public LocalDate getPieceDate() {
        return pieceDate;
    }

    public Optional<Integer> getPieceNumber() {
        return Optional.ofNullable(pieceNumber);
    }

    public Optional<String> getExternalReference() {
        return Optional.ofNullable(externalReference);
    }

    public JournalEntryStatus getStatus() {
        return status;
    }

    public Optional<JournalEntryId> getOriginalEntryId() {
        return Optional.ofNullable(originalEntryId);
    }

    public EntityId getCreatedByUserId() {
        return createdByUserId;
    }

    public List<JournalEntryLine> getLines() {
        return lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof JournalEntry other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
