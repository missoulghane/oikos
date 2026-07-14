package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.RemoveUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.RemoveUnitOwnershipUseCase;
import com.architek.oikos.property.domain.exception.UnitOwnershipNotFoundException;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;

@Component
public class RemoveUnitOwnershipService implements RemoveUnitOwnershipUseCase {

    private final UnitOwnershipRepository unitOwnershipRepository;

    public RemoveUnitOwnershipService(UnitOwnershipRepository unitOwnershipRepository) {
        this.unitOwnershipRepository = unitOwnershipRepository;
    }

    @Override
    @Transactional
    public void remove(RemoveUnitOwnershipCommand command) {
        if (unitOwnershipRepository.findById(command.id()).isEmpty()) {
            throw new UnitOwnershipNotFoundException(command.id());
        }
        unitOwnershipRepository.deleteById(command.id());
    }
}
