package com.architek.oikos.property.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.domain.exception.PartyAlreadyOwnsUnitException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.exception.OwnershipShareExceededException;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;

@Component
public class AddUnitOwnershipService implements AddUnitOwnershipUseCase {

    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitRepository unitRepository;

    public AddUnitOwnershipService(UnitOwnershipRepository unitOwnershipRepository, UnitRepository unitRepository) {
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    @Transactional
    public UnitOwnershipId add(AddUnitOwnershipCommand command) {
        unitRepository.findById(command.unitId()).orElseThrow(() -> new UnitNotFoundException(command.unitId()));

        if (unitOwnershipRepository.existsByUnitIdAndPartyId(command.unitId(), command.partyId())) {
            throw new PartyAlreadyOwnsUnitException();
        }

        List<UnitOwnership> existing = unitOwnershipRepository.findAllByUnitId(command.unitId());
        BigDecimal existingTotal = existing.stream()
                .map(unitOwnership -> unitOwnership.getOwnershipShare().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (existingTotal.add(command.ownershipShare()).compareTo(new BigDecimal("100")) > 0) {
            throw new OwnershipShareExceededException(command.unitId());
        }

        UnitOwnership unitOwnership = UnitOwnership.create(UnitOwnershipId.newId(), command.unitId(), command.partyId(),
                OwnershipShare.of(command.ownershipShare()));
        return unitOwnershipRepository.save(unitOwnership).getId();
    }
}
