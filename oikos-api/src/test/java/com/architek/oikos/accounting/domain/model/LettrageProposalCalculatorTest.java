package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountAllocationId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class LettrageProposalCalculatorTest {

    private static final UnitAccountId UNIT_ACCOUNT_ID = UnitAccountId.newId();
    private static final AccountingExerciseId EXERCISE_ID = AccountingExerciseId.newId();

    private static UnitAccountMovement fundCall(LocalDate date, String amount) {
        return UnitAccountMovement.create(UnitAccountMovementId.newId(), EXERCISE_ID, UNIT_ACCOUNT_ID, date,
                UnitAccountMovementType.FUND_CALL, UnitAccountMovementDirection.DEBIT, Amount.of(new BigDecimal(amount)),
                null, "Appel de cotisation", null);
    }

    private static UnitAccountMovement payment(LocalDate date, String amount) {
        return UnitAccountMovement.create(UnitAccountMovementId.newId(), EXERCISE_ID, UNIT_ACCOUNT_ID, date,
                UnitAccountMovementType.PAYMENT, UnitAccountMovementDirection.CREDIT, Amount.of(new BigDecimal(amount)),
                null, "Paiement propriétaire", null);
    }

    private static UnitAccountMovement creditRegularization(LocalDate date, String amount) {
        return UnitAccountMovement.create(UnitAccountMovementId.newId(), EXERCISE_ID, UNIT_ACCOUNT_ID, date,
                UnitAccountMovementType.REGULARIZATION, UnitAccountMovementDirection.CREDIT,
                Amount.of(new BigDecimal(amount)), null, "Avoir", "Correction erreur de tantièmes");
    }

    private static UnitAccountAllocation allocation(UnitAccountMovement debit, UnitAccountMovement credit, String amount) {
        return UnitAccountAllocation.create(UnitAccountAllocationId.newId(), UNIT_ACCOUNT_ID, debit.getId(),
                credit.getId(), Amount.of(new BigDecimal(amount)), debit.getDate(), EntityId.newId());
    }

    @Test
    void exact_match_settles_the_fund_call_entirely() {
        UnitAccountMovement debit = fundCall(LocalDate.of(2026, 3, 1), "300");
        UnitAccountMovement credit = payment(LocalDate.of(2026, 3, 5), "300");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit, credit), List.of());

        assertThat(proposal.lines()).hasSize(1);
        assertThat(proposal.lines().get(0).debitMovementId()).isEqualTo(debit.getId());
        assertThat(proposal.lines().get(0).creditMovementId()).isEqualTo(credit.getId());
        assertThat(proposal.lines().get(0).amount()).isEqualByComparingTo("300");
        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("0");
        assertThat(proposal.totalUnmatchedCredit()).isEqualByComparingTo("0");
    }

    @Test
    void partial_payment_leaves_the_fund_call_partly_unsettled() {
        UnitAccountMovement debit = fundCall(LocalDate.of(2026, 3, 1), "300");
        UnitAccountMovement credit = payment(LocalDate.of(2026, 3, 5), "150");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit, credit), List.of());

        assertThat(proposal.lines()).hasSize(1);
        assertThat(proposal.lines().get(0).amount()).isEqualByComparingTo("150");
        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("150");
        assertThat(proposal.totalUnmatchedCredit()).isEqualByComparingTo("0");
    }

    @Test
    void one_payment_covers_two_fund_calls_oldest_first() {
        UnitAccountMovement debit1 = fundCall(LocalDate.of(2026, 1, 1), "200");
        UnitAccountMovement debit2 = fundCall(LocalDate.of(2026, 2, 1), "200");
        UnitAccountMovement credit = payment(LocalDate.of(2026, 3, 1), "300");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit2, debit1, credit), List.of());

        assertThat(proposal.lines()).hasSize(2);
        assertThat(proposal.lines().get(0).debitMovementId()).isEqualTo(debit1.getId());
        assertThat(proposal.lines().get(0).amount()).isEqualByComparingTo("200");
        assertThat(proposal.lines().get(1).debitMovementId()).isEqualTo(debit2.getId());
        assertThat(proposal.lines().get(1).amount()).isEqualByComparingTo("100");
        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("100");
        assertThat(proposal.totalUnmatchedCredit()).isEqualByComparingTo("0");
    }

    @Test
    void two_payments_cover_one_fund_call() {
        UnitAccountMovement debit = fundCall(LocalDate.of(2026, 3, 1), "300");
        UnitAccountMovement credit1 = payment(LocalDate.of(2026, 3, 5), "100");
        UnitAccountMovement credit2 = payment(LocalDate.of(2026, 3, 10), "200");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit, credit2, credit1), List.of());

        assertThat(proposal.lines()).hasSize(2);
        assertThat(proposal.lines().get(0).creditMovementId()).isEqualTo(credit1.getId());
        assertThat(proposal.lines().get(0).amount()).isEqualByComparingTo("100");
        assertThat(proposal.lines().get(1).creditMovementId()).isEqualTo(credit2.getId());
        assertThat(proposal.lines().get(1).amount()).isEqualByComparingTo("200");
        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("0");
    }

    @Test
    void existing_allocations_reduce_what_is_still_available_or_outstanding() {
        UnitAccountMovement debit = fundCall(LocalDate.of(2026, 3, 1), "300");
        UnitAccountMovement credit = payment(LocalDate.of(2026, 3, 5), "300");
        UnitAccountAllocation existing = allocation(debit, credit, "300");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit, credit), List.of(existing));

        assertThat(proposal.lines()).isEmpty();
        assertThat(proposal.unsettledDebits()).isEmpty();
        assertThat(proposal.unallocatedCredits()).isEmpty();
    }

    @Test
    void credit_exceeding_the_debt_leaves_an_advance() {
        UnitAccountMovement debit = fundCall(LocalDate.of(2026, 3, 1), "300");
        UnitAccountMovement credit = payment(LocalDate.of(2026, 3, 5), "500");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit, credit), List.of());

        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("0");
        assertThat(proposal.totalUnmatchedCredit()).isEqualByComparingTo("200");
    }

    @Test
    void insufficient_credit_leaves_the_fund_call_outstanding() {
        UnitAccountMovement debit = fundCall(LocalDate.of(2026, 3, 1), "300");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit), List.of());

        assertThat(proposal.lines()).isEmpty();
        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("300");
        assertThat(proposal.unsettledDebits()).hasSize(1);
    }

    @Test
    void a_credit_regularization_can_settle_a_fund_call_like_a_payment() {
        UnitAccountMovement debit = fundCall(LocalDate.of(2026, 3, 1), "80");
        UnitAccountMovement credit = creditRegularization(LocalDate.of(2026, 3, 2), "80");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debit, credit), List.of());

        assertThat(proposal.lines()).hasSize(1);
        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("0");
    }

    @Test
    void a_debit_regularization_is_never_treated_as_a_fund_call_to_settle() {
        UnitAccountMovement debitRegularization = UnitAccountMovement.create(UnitAccountMovementId.newId(),
                EXERCISE_ID, UNIT_ACCOUNT_ID, LocalDate.of(2026, 3, 1), UnitAccountMovementType.REGULARIZATION,
                UnitAccountMovementDirection.DEBIT, Amount.of(new BigDecimal("50")), null, "Pénalité", "Retard");
        UnitAccountMovement credit = payment(LocalDate.of(2026, 3, 5), "50");

        LettrageProposal proposal = LettrageProposalCalculator.compute(List.of(debitRegularization, credit), List.of());

        assertThat(proposal.lines()).isEmpty();
        assertThat(proposal.totalUnmatchedDebit()).isEqualByComparingTo("0");
        assertThat(proposal.totalUnmatchedCredit()).isEqualByComparingTo("50");
    }
}
