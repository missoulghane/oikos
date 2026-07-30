package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.PendingLettrageView;
import com.architek.oikos.accounting.application.port.in.ListPendingLettragesByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListPendingLettragesByPropertyQuery;
import com.architek.oikos.accounting.domain.model.LettrageAllocationLine;
import com.architek.oikos.accounting.domain.model.LettrageProposal;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;

@Component
public class ListPendingLettragesByPropertyService implements ListPendingLettragesByPropertyUseCase {

    private final UnitAccountRepository unitAccountRepository;
    private final ResolveLettrageProposalService resolveLettrageProposalService;

    public ListPendingLettragesByPropertyService(UnitAccountRepository unitAccountRepository,
                                                  ResolveLettrageProposalService resolveLettrageProposalService) {
        this.unitAccountRepository = unitAccountRepository;
        this.resolveLettrageProposalService = resolveLettrageProposalService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingLettrageView> list(ListPendingLettragesByPropertyQuery query) {
        return unitAccountRepository.findAllByPropertyId(query.propertyId()).stream()
                .map(this::toPendingViewOrNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private PendingLettrageView toPendingViewOrNull(UnitAccount unitAccount) {
        LettrageProposal proposal = resolveLettrageProposalService.resolve(unitAccount.getId()).proposal();
        if (proposal.lines().isEmpty()) {
            return null;
        }
        BigDecimal proposedAmount = proposal.lines().stream().map(LettrageAllocationLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PendingLettrageView(unitAccount.getUnitId(), proposedAmount, proposal.totalUnmatchedDebit(),
                proposal.totalUnmatchedCredit());
    }
}
