package com.architek.oikos.installment.application.command;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RegularizeUnitInstallmentsCommand(EntityId propertyId, EntityId unitId, LocalDate pieceDate,
                                                  EntityId createdByUserId) {
}
