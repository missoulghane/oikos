package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.LettrageProposalView;
import com.architek.oikos.accounting.application.query.GetUnitLettrageProposalQuery;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.LettrageAllocationLine;
import com.architek.oikos.accounting.domain.model.LettrageMovementLine;
import com.architek.oikos.accounting.domain.model.LettrageProposal;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetUnitLettrageProposalServiceTest {

    @Mock
    private UnitAccountRepository unitAccountRepository;

    @Mock
    private ResolveLettrageProposalService resolveLettrageProposalService;

    private GetUnitLettrageProposalService newService() {
        return new GetUnitLettrageProposalService(unitAccountRepository, resolveLettrageProposalService);
    }

    @Test
    void the_view_reflects_the_resolved_proposal() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        UnitAccountId unitAccountId = UnitAccountId.newId();
        UnitAccount unitAccount = UnitAccount.reconstruct(unitAccountId, unitId, propertyId, BigDecimal.ZERO,
                Instant.now());

        UnitAccountMovement debit = UnitAccountMovement.create(UnitAccountMovementId.newId(),
                AccountingExerciseId.newId(), unitAccountId, LocalDate.of(2026, 3, 1), UnitAccountMovementType.FUND_CALL,
                UnitAccountMovementDirection.DEBIT, Amount.of(new BigDecimal("300")), null, "Appel de cotisation", null);
        UnitAccountMovement credit = UnitAccountMovement.create(UnitAccountMovementId.newId(),
                AccountingExerciseId.newId(), unitAccountId, LocalDate.of(2026, 3, 5), UnitAccountMovementType.PAYMENT,
                UnitAccountMovementDirection.CREDIT, Amount.of(new BigDecimal("150")), null, "Paiement propriétaire",
                null);

        LettrageProposal proposal = new LettrageProposal(
                List.of(new LettrageMovementLine(debit.getId(), debit.getDate(), new BigDecimal("150"))), List.of(),
                List.of(new LettrageAllocationLine(debit.getId(), credit.getId(), new BigDecimal("150"))),
                new BigDecimal("150"), BigDecimal.ZERO);

        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.of(unitAccount));
        when(resolveLettrageProposalService.resolve(unitAccountId))
                .thenReturn(new LettrageContext(List.of(debit, credit), proposal));

        LettrageProposalView view = newService().get(new GetUnitLettrageProposalQuery(unitId));

        assertThat(view.unitId()).isEqualTo(unitId);
        assertThat(view.unsettledDebits()).hasSize(1);
        assertThat(view.unsettledDebits().get(0).remainingAmount()).isEqualByComparingTo("150");
        assertThat(view.proposedLines()).hasSize(1);
        assertThat(view.totalProposedAmount()).isEqualByComparingTo("150");
        assertThat(view.totalUnmatchedDebit()).isEqualByComparingTo("150");
    }

    @Test
    void a_unit_with_no_account_is_rejected() {
        EntityId unitId = EntityId.newId();
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().get(new GetUnitLettrageProposalQuery(unitId)))
                .isInstanceOf(UnitAccountNotFoundException.class);
    }
}
