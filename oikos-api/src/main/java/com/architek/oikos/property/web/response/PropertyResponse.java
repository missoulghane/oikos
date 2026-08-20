package com.architek.oikos.property.web.response;

import java.math.BigDecimal;

import com.architek.oikos.property.application.dto.PropertyView;

public record PropertyResponse(String id, String name, String address, String city, String duesCalculationMode,
                               BigDecimal projectedBudget) {

    public static PropertyResponse from(PropertyView view) {
        return new PropertyResponse(view.id().toString(), view.name(), view.address(), view.city(),
                view.duesCalculationMode().name(), view.projectedBudget());
    }
}
