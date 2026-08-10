package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * P9 (spec &sect;9): closes the income statement (classes 6/7 zeroed out,
 * net result booked to the "resultat de l'exercice" account) and generates
 * the a-nouveaux opening lines for balance-sheet accounts (classes 1-5) to
 * carry their ending balance into the next exercise. Pure function over
 * pre-fetched balances (the caller resolves them via the repositories, and
 * is responsible for actually creating/posting the resulting JournalEntry
 * on the AN journal for the next exercise - a later phase); like
 * SharesApportionment, deterministic and stateless.
 *
 * <p>balance is the account's net amount expressed <em>in its own
 * normalSide direction</em>: positive means the account sits on its normal
 * side (the common case), negative means it is abnormally on the opposite
 * side (e.g. an overdrawn bank account, spec &sect;11's "SOLDE_NEGATIF"
 * note) - closing/carry-forward both handle this correctly by direction
 * arithmetic rather than assuming a sign.
 */
public final class ExerciseClosingCalculator {

    private ExerciseClosingCalculator() {
    }

    public record AccountBalance(LedgerAccountId accountId, EntryDirection normalSide, BigDecimal balance) {
    }

    public record ClosingResult(List<JournalEntryLine> lines, BigDecimal netResult) {
    }

    /**
     * Zeroes out every class 6/7 balance and books the net result
     * (income - expense) to resultAccountId. Requires balances.size() + 1
     * line ids (only as many are consumed as lines actually produced - zero
     * balances produce no line, and an exactly-zero net result produces no
     * result line either).
     */
    public static ClosingResult closeIncomeStatement(List<AccountBalance> incomeStatementBalances,
                                                       LedgerAccountId resultAccountId,
                                                       List<JournalEntryLineId> lineIds) {
        Iterator<JournalEntryLineId> ids = lineIds.iterator();
        List<JournalEntryLine> lines = new ArrayList<>();
        BigDecimal netResult = BigDecimal.ZERO;
        for (AccountBalance accountBalance : incomeStatementBalances) {
            netResult = accountBalance.normalSide() == EntryDirection.CREDIT
                    ? netResult.add(accountBalance.balance())
                    : netResult.subtract(accountBalance.balance());
            closingLine(accountBalance, ids).ifPresent(lines::add);
        }
        if (netResult.signum() != 0) {
            EntryDirection direction = netResult.signum() > 0 ? EntryDirection.CREDIT : EntryDirection.DEBIT;
            lines.add(JournalEntryLine.of(ids.next(), resultAccountId, null, null, direction,
                    Amount.of(netResult.abs()), "Resultat de l'exercice"));
        }
        return new ClosingResult(lines, netResult);
    }

    /**
     * Carries each non-zero balance-sheet (class 1-5) balance forward as an
     * opening line on the next exercise's AN journal entry - same direction
     * as the closing balance (not flipped, unlike a P10 reversal), since
     * this recreates the balance, it does not cancel it.
     */
    public static List<JournalEntryLine> generateOpeningBalances(List<AccountBalance> balanceSheetBalances,
                                                                   List<JournalEntryLineId> lineIds) {
        Iterator<JournalEntryLineId> ids = lineIds.iterator();
        List<JournalEntryLine> lines = new ArrayList<>();
        for (AccountBalance accountBalance : balanceSheetBalances) {
            openingLine(accountBalance, ids).ifPresent(lines::add);
        }
        return lines;
    }

    private static Optional<JournalEntryLine> closingLine(AccountBalance accountBalance,
                                                                      Iterator<JournalEntryLineId> ids) {
        if (accountBalance.balance().signum() == 0) {
            return Optional.empty();
        }
        EntryDirection closingDirection = accountBalance.balance().signum() > 0
                ? opposite(accountBalance.normalSide())
                : accountBalance.normalSide();
        return Optional.of(JournalEntryLine.of(ids.next(), accountBalance.accountId(), null, null,
                closingDirection, Amount.of(accountBalance.balance().abs()), "Cloture d'exercice"));
    }

    private static Optional<JournalEntryLine> openingLine(AccountBalance accountBalance,
                                                                      Iterator<JournalEntryLineId> ids) {
        if (accountBalance.balance().signum() == 0) {
            return Optional.empty();
        }
        EntryDirection openingDirection = accountBalance.balance().signum() > 0
                ? accountBalance.normalSide()
                : opposite(accountBalance.normalSide());
        return Optional.of(JournalEntryLine.of(ids.next(), accountBalance.accountId(), null, null,
                openingDirection, Amount.of(accountBalance.balance().abs()), "A-nouveaux"));
    }

    private static EntryDirection opposite(EntryDirection direction) {
        return direction == EntryDirection.DEBIT ? EntryDirection.CREDIT : EntryDirection.DEBIT;
    }
}
