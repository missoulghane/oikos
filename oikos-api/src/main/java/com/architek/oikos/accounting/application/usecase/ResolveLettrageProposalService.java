package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.LettrageProposal;
import com.architek.oikos.accounting.domain.model.LettrageProposalCalculator;
import com.architek.oikos.accounting.domain.model.UnitAccountAllocation;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountAllocationRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;

/**
 * Loads a unit account's movements/allocations and computes its current
 * lettrage proposal - shared by every read/write use case below so the
 * "load then compute" glue isn't repeated three times.
 */
@Component
class ResolveLettrageProposalService {

    private final UnitAccountMovementRepository unitAccountMovementRepository;
    private final UnitAccountAllocationRepository unitAccountAllocationRepository;

    ResolveLettrageProposalService(UnitAccountMovementRepository unitAccountMovementRepository,
                                    UnitAccountAllocationRepository unitAccountAllocationRepository) {
        this.unitAccountMovementRepository = unitAccountMovementRepository;
        this.unitAccountAllocationRepository = unitAccountAllocationRepository;
    }

    LettrageContext resolve(UnitAccountId unitAccountId) {
        List<UnitAccountMovement> movements = unitAccountMovementRepository.findAllByUnitAccountId(unitAccountId);
        List<UnitAccountAllocation> allocations = unitAccountAllocationRepository.findAllByUnitAccountId(unitAccountId);
        LettrageProposal proposal = LettrageProposalCalculator.compute(movements, allocations);
        return new LettrageContext(movements, proposal);
    }
}
