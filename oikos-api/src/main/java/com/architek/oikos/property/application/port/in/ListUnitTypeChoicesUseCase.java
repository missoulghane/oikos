package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.domain.model.UnitTypeChoice;

/** The unit types on offer, for the sign-up wizard and a property's configuration. */
public interface ListUnitTypeChoicesUseCase {

    List<UnitTypeChoice> listUnitTypeChoices();
}
