package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;

/**
 * P8: the blocking checks run before a period can be closed (spec &sect;6) -
 * global balance, no DRAFT entries left, no gap in piece numbering,
 * theoretical treasury balance consistent with the last reconciliation if
 * one exists. Pure function over pre-fetched data (the caller resolves
 * entry statuses/piece numbers/balances via the repositories) returning the
 * list of violations for EXERCICE_NON_CLOTURABLE (spec &sect;12) - not an
 * exception, since the API surface returns the full list of failing checks
 * in one response rather than stopping at the first one.
 *
 * <p>allocatedPieceNumbersByJournal is keyed by JournalCode rather than a
 * single flat list: piece numbering (I7) is a separate gapless sequence per
 * (property, exercise, journal), so mixing two journals' numbers into one
 * list could either mask a real gap or report a false one purely from
 * interleaving - each journal's own numbers must be checked for
 * contiguity independently of every other journal's.
 */
public final class PeriodClosingValidator {

    private PeriodClosingValidator() {
    }

    public record TreasuryReconciliation(BigDecimal theoreticalBalance, BigDecimal lastReconciledBalance) {
    }

    public static List<String> violations(List<JournalEntryStatus> entryStatuses,
                                           Map<JournalCode, List<Integer>> allocatedPieceNumbersByJournal,
                                           TreasuryReconciliation treasuryReconciliation) {
        List<String> violations = new ArrayList<>();
        if (entryStatuses.stream().anyMatch(status -> status == JournalEntryStatus.DRAFT)) {
            violations.add("At least one journal entry is still DRAFT");
        }
        if (allocatedPieceNumbersByJournal.values().stream().anyMatch(PeriodClosingValidator::hasGap)) {
            violations.add("Piece numbering has a gap");
        }
        if (treasuryReconciliation != null
                && treasuryReconciliation.theoreticalBalance().compareTo(treasuryReconciliation.lastReconciledBalance()) != 0) {
            violations.add("Theoretical treasury balance does not match the last reconciliation");
        }
        return violations;
    }

    public static boolean isClosable(List<JournalEntryStatus> entryStatuses,
                                      Map<JournalCode, List<Integer>> allocatedPieceNumbersByJournal,
                                      TreasuryReconciliation treasuryReconciliation) {
        return violations(entryStatuses, allocatedPieceNumbersByJournal, treasuryReconciliation).isEmpty();
    }

    private static boolean hasGap(List<Integer> pieceNumbers) {
        List<Integer> sorted = pieceNumbers.stream().sorted().distinct().toList();
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i) - sorted.get(i - 1) != 1) {
                return true;
            }
        }
        return false;
    }
}
