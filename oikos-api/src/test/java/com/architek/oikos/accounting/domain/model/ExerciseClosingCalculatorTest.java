package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.model.ExerciseClosingCalculator.AccountBalance;
import com.architek.oikos.accounting.domain.model.ExerciseClosingCalculator.ClosingResult;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;

class ExerciseClosingCalculatorTest {

    @Test
    void P9_golden_dataset_closes_the_income_statement_to_a_290_profit() {
        LedgerAccountId cotisations = LedgerAccountId.newId();
        LedgerAccountId fournitures = LedgerAccountId.newId();
        LedgerAccountId salaires = LedgerAccountId.newId();
        LedgerAccountId agios = LedgerAccountId.newId();
        LedgerAccountId resultAccountId = LedgerAccountId.newId();

        List<AccountBalance> balances = List.of(
                new AccountBalance(cotisations, EntryDirection.CREDIT, new BigDecimal("3000.00")),
                new AccountBalance(fournitures, EntryDirection.DEBIT, new BigDecimal("150.00")),
                new AccountBalance(salaires, EntryDirection.DEBIT, new BigDecimal("2500.00")),
                new AccountBalance(agios, EntryDirection.DEBIT, new BigDecimal("60.00")));

        List<JournalEntryLineId> lineIds = List.of(JournalEntryLineId.newId(), JournalEntryLineId.newId(),
                JournalEntryLineId.newId(), JournalEntryLineId.newId(), JournalEntryLineId.newId());

        ClosingResult result = ExerciseClosingCalculator.closeIncomeStatement(balances, resultAccountId, lineIds);

        assertThat(result.netResult()).isEqualByComparingTo("290.00");
        // 4 closing lines (one per account) + 1 result line.
        assertThat(result.lines()).hasSize(5);
        assertThat(result.lines()).filteredOn(line -> line.getLedgerAccountId().equals(cotisations))
                .singleElement().satisfies(line -> {
                    assertThat(line.getDirection()).isEqualTo(EntryDirection.DEBIT);
                    assertThat(line.getAmount().value()).isEqualByComparingTo("3000.00");
                });
        assertThat(result.lines()).filteredOn(line -> line.getLedgerAccountId().equals(resultAccountId))
                .singleElement().satisfies(line -> {
                    assertThat(line.getDirection()).isEqualTo(EntryDirection.CREDIT);
                    assertThat(line.getAmount().value()).isEqualByComparingTo("290.00");
                });
    }

    @Test
    void a_zero_net_result_produces_no_result_line() {
        LedgerAccountId income = LedgerAccountId.newId();
        LedgerAccountId expense = LedgerAccountId.newId();

        ClosingResult result = ExerciseClosingCalculator.closeIncomeStatement(List.of(
                new AccountBalance(income, EntryDirection.CREDIT, new BigDecimal("100.00")),
                new AccountBalance(expense, EntryDirection.DEBIT, new BigDecimal("100.00"))),
                LedgerAccountId.newId(), List.of(JournalEntryLineId.newId(), JournalEntryLineId.newId(), JournalEntryLineId.newId()));

        assertThat(result.netResult()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.lines()).hasSize(2);
    }

    @Test
    void golden_dataset_note_an_overdrawn_treasury_account_carries_forward_as_a_credit_opening_balance() {
        LedgerAccountId bank = LedgerAccountId.newId();

        List<JournalEntryLine> opening = ExerciseClosingCalculator.generateOpeningBalances(List.of(
                new AccountBalance(bank, EntryDirection.DEBIT, new BigDecimal("-1260.00"))),
                List.of(JournalEntryLineId.newId()));

        assertThat(opening).singleElement().satisfies(line -> {
            assertThat(line.getDirection()).isEqualTo(EntryDirection.CREDIT);
            assertThat(line.getAmount().value()).isEqualByComparingTo("1260.00");
        });
    }

    @Test
    void a_normal_positive_balance_carries_forward_on_its_own_normal_side() {
        LedgerAccountId cash = LedgerAccountId.newId();

        List<JournalEntryLine> opening = ExerciseClosingCalculator.generateOpeningBalances(List.of(
                new AccountBalance(cash, EntryDirection.DEBIT, new BigDecimal("1350.00"))),
                List.of(JournalEntryLineId.newId()));

        assertThat(opening).singleElement().satisfies(line -> {
            assertThat(line.getDirection()).isEqualTo(EntryDirection.DEBIT);
            assertThat(line.getAmount().value()).isEqualByComparingTo("1350.00");
        });
    }
}
