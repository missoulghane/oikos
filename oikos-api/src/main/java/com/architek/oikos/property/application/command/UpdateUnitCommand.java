package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.UnitId;

/** What a lot's edit form changes: its number, its floor (null = not known) and its shares. */
public record UpdateUnitCommand(UnitId id, String unitNumber, Integer floor, BigDecimal shares) {
}
