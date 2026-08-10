package com.architek.oikos.accounting.application.command;

import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PostFundCallJournalEntryCommand(EntityId propertyId, LocalDate pieceDate, String externalReference,
                                               EntityId createdByUserId, List<FundCallLineCommand> lines) {
}
