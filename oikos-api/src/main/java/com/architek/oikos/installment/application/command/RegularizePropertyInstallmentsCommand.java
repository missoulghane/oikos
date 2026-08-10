package com.architek.oikos.installment.application.command;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RegularizePropertyInstallmentsCommand(EntityId propertyId, LocalDate pieceDate,
                                                      EntityId createdByUserId) {
}
