package com.architek.oikos.property.web.response;

import com.architek.oikos.property.domain.model.UnitTypeChoice;

/** A unit type catalog row as the clients read it. */
public record UnitTypeChoiceResponse(String name, String plural, String description, int position) {

    public static UnitTypeChoiceResponse from(UnitTypeChoice choice) {
        return new UnitTypeChoiceResponse(choice.getName(), choice.getPlural(), choice.getDescription(),
                choice.getPosition());
    }
}
