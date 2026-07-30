package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.LettrageMovementView;
import com.architek.oikos.accounting.application.dto.LettrageProposalLineView;
import com.architek.oikos.accounting.application.dto.LettrageProposalView;
import com.architek.oikos.accounting.application.port.in.GetUnitLettrageProposalUseCase;
import com.architek.oikos.accounting.application.query.GetUnitLettrageProposalQuery;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.LettrageMovementLine;
import com.architek.oikos.accounting.domain.model.LettrageProposal;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class GetUnitLettrageProposalService implements GetUnitLettrageProposalUseCase {

    private final UnitAccountRepository unitAccountRepository;
    private final ResolveLettrageProposalService resolveLettrageProposalService;

    public GetUnitLettrageProposalService(UnitAccountRepository unitAccountRepository,
                                           ResolveLettrageProposalService resolveLettrageProposalService) {
        this.unitAccountRepository = unitAccountRepository;
        this.resolveLettrageProposalService = resolveLettrageProposalService;
    }

    @Override
    @Transactional(readOnly = true)
    public LettrageProposalView get(GetUnitLettrageProposalQuery query) {
        UnitAccount unitAccount = unitAccountRepository.findByUnitId(query.unitId())
                .orElseThrow(() -> UnitAccountNotFoundException.forUnit(query.unitId()));
        LettrageContext context = resolveLettrageProposalService.resolve(unitAccount.getId());
        return toView(query.unitId(), context);
    }

    private static LettrageProposalView toView(EntityId unitId, LettrageContext context) {
        Map<UnitAccountMovementId, UnitAccountMovement> byId = context.movements().stream()
                .collect(Collectors.toMap(UnitAccountMovement::getId, Function.identity()));
        LettrageProposal proposal = context.proposal();

        List<LettrageMovementView> unsettledDebits = proposal.unsettledDebits().stream()
                .map(line -> toMovementView(line, byId))
                .toList();
        List<LettrageMovementView> unallocatedCredits = proposal.unallocatedCredits().stream()
                .map(line -> toMovementView(line, byId))
                .toList();
        List<LettrageProposalLineView> proposedLines = proposal.lines().stream()
                .map(LettrageProposalLineView::from)
                .toList();
        BigDecimal totalProposedAmount = proposedLines.stream().map(LettrageProposalLineView::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new LettrageProposalView(unitId, unsettledDebits, unallocatedCredits, proposedLines,
                totalProposedAmount, proposal.totalUnmatchedDebit(), proposal.totalUnmatchedCredit());
    }

    private static LettrageMovementView toMovementView(LettrageMovementLine line,
                                                        Map<UnitAccountMovementId, UnitAccountMovement> byId) {
        return LettrageMovementView.of(byId.get(line.movementId()), line.amount());
    }
}
