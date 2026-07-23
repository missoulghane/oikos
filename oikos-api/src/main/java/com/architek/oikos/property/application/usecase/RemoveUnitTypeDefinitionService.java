package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.RemoveUnitTypeDefinitionCommand;
import com.architek.oikos.property.application.port.in.RemoveUnitTypeDefinitionUseCase;
import com.architek.oikos.property.domain.exception.UnitTypeDefinitionNotFoundException;
import com.architek.oikos.property.domain.exception.UnitTypeInUseException;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;

/**
 * La ligne "OTHERS" seedee a la creation de la property n'est pas protegee:
 * elle peut etre retiree comme n'importe quel autre type, tant qu'aucun Unit
 * ne la reference (meme regle que pour tout autre type). La ligne de prix
 * associee (unit_type_pricing), elle, est supprimee automatiquement par la
 * DB (ON DELETE CASCADE) puisqu'un prix sans type n'a pas de sens.
 */
@Component
public class RemoveUnitTypeDefinitionService implements RemoveUnitTypeDefinitionUseCase {

    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final UnitRepository unitRepository;

    public RemoveUnitTypeDefinitionService(UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                             UnitRepository unitRepository) {
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    @Transactional
    public void remove(RemoveUnitTypeDefinitionCommand command) {
        unitTypeDefinitionRepository.findById(command.id())
                .orElseThrow(() -> new UnitTypeDefinitionNotFoundException(command.id()));

        if (unitRepository.existsByUnitTypeId(command.id())) {
            throw new UnitTypeInUseException(command.id());
        }

        unitTypeDefinitionRepository.deleteById(command.id());
    }
}
