package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.UpdateUnitSharesCommand;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.UpdateUnitSharesUseCase;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.Shares;

@Component
public class UpdateUnitSharesService implements UpdateUnitSharesUseCase {

    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    public UpdateUnitSharesService(UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository,
                                    UnitTypeDefinitionRepository unitTypeDefinitionRepository) {
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
    }

    @Override
    @Transactional
    public UnitView updateShares(UpdateUnitSharesCommand command) {
        Unit unit = unitRepository.findById(command.id())
                .orElseThrow(() -> new UnitNotFoundException(command.id()));
        Unit updated = unitRepository.save(unit.withShares(Shares.of(command.shares())));

        boolean hasCoproprietaires = !unitOwnershipRepository.findAllByUnitId(updated.getId()).isEmpty();
        String unitTypeName = unitTypeDefinitionRepository.findById(updated.getUnitTypeId())
                .map(UnitTypeDefinition::getName)
                .orElse(null);
        return UnitView.from(updated, hasCoproprietaires, unitTypeName);
    }
}
