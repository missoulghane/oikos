package com.architek.oikos.property.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.port.in.ListUnitTypeChoicesUseCase;
import com.architek.oikos.property.domain.model.UnitTypeChoice;
import com.architek.oikos.property.domain.repository.UnitTypeChoiceRepository;

/** Active rows only, in display order. */
@Component
public class ListUnitTypeChoicesService implements ListUnitTypeChoicesUseCase {

    private final UnitTypeChoiceRepository unitTypeChoiceRepository;

    public ListUnitTypeChoicesService(UnitTypeChoiceRepository unitTypeChoiceRepository) {
        this.unitTypeChoiceRepository = unitTypeChoiceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitTypeChoice> listUnitTypeChoices() {
        return unitTypeChoiceRepository.findAllActive();
    }
}
