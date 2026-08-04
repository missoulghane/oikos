package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.SetProjectedBudgetCommand;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.SetProjectedBudgetUseCase;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.ProjectedBudget;

@Component
public class SetProjectedBudgetService implements SetProjectedBudgetUseCase {

    private final PropertyRepository propertyRepository;

    public SetProjectedBudgetService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public PropertyView setProjectedBudget(SetProjectedBudgetCommand command) {
        Property property = propertyRepository.findById(command.id())
                .orElseThrow(() -> new PropertyNotFoundException(command.id()));
        Property updated = propertyRepository.save(
                property.withProjectedBudget(ProjectedBudget.of(command.projectedBudget())));
        return PropertyView.from(updated);
    }
}
