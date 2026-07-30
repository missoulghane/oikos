package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.PendingLettrageView;
import com.architek.oikos.accounting.application.query.ListPendingLettragesByPropertyQuery;
import com.architek.oikos.accounting.domain.model.LettrageAllocationLine;
import com.architek.oikos.accounting.domain.model.LettrageProposal;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListPendingLettragesByPropertyServiceTest {

    @Mock
    private UnitAccountRepository unitAccountRepository;

    @Mock
    private ResolveLettrageProposalService resolveLettrageProposalService;

    private ListPendingLettragesByPropertyService newService() {
        return new ListPendingLettragesByPropertyService(unitAccountRepository, resolveLettrageProposalService);
    }

    @Test
    void only_units_with_a_non_empty_proposal_are_returned() {
        EntityId propertyId = EntityId.newId();
        EntityId unitWithPendingId = EntityId.newId();
        EntityId unitUpToDateId = EntityId.newId();
        UnitAccountId unitWithPendingAccountId = UnitAccountId.newId();
        UnitAccountId unitUpToDateAccountId = UnitAccountId.newId();
        UnitAccount unitWithPending = UnitAccount.reconstruct(unitWithPendingAccountId, unitWithPendingId, propertyId,
                BigDecimal.ZERO, Instant.now());
        UnitAccount unitUpToDate = UnitAccount.reconstruct(unitUpToDateAccountId, unitUpToDateId, propertyId,
                BigDecimal.ZERO, Instant.now());

        LettrageProposal pendingProposal = new LettrageProposal(List.of(), List.of(),
                List.of(new LettrageAllocationLine(UnitAccountMovementId.newId(), UnitAccountMovementId.newId(),
                        new BigDecimal("150"))),
                new BigDecimal("50"), BigDecimal.ZERO);
        LettrageProposal emptyProposal = new LettrageProposal(List.of(), List.of(), List.of(), BigDecimal.ZERO,
                BigDecimal.ZERO);

        when(unitAccountRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(unitWithPending, unitUpToDate));
        when(resolveLettrageProposalService.resolve(unitWithPendingAccountId))
                .thenReturn(new LettrageContext(List.of(), pendingProposal));
        when(resolveLettrageProposalService.resolve(unitUpToDateAccountId))
                .thenReturn(new LettrageContext(List.of(), emptyProposal));

        List<PendingLettrageView> result = newService().list(new ListPendingLettragesByPropertyQuery(propertyId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).unitId()).isEqualTo(unitWithPendingId);
        assertThat(result.get(0).proposedAmount()).isEqualByComparingTo("150");
        assertThat(result.get(0).remainingUnmatchedDebitAfter()).isEqualByComparingTo("50");
    }
}
