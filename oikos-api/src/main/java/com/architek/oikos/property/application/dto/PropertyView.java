package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.ProjectedBudget;
import com.architek.oikos.property.domain.valueobject.PropertyId;

public record PropertyView(PropertyId id, String name, String address, DuesCalculationMode duesCalculationMode,
                           BigDecimal projectedBudget) {

    public static PropertyView from(Property property) {
        return new PropertyView(property.getId(), property.getName(), property.getAddress(),
                property.getDuesCalculationMode(),
                property.getProjectedBudget().map(ProjectedBudget::value).orElse(null));
    }
}
