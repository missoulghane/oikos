package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Imputes part (or all) of a unit's already-recorded advance onto its
 * unpaid fund calls, after the fact - the caller (installment module) has
 * already run the FIFO allocation and just needs the resulting DEBIT
 * avance / CREDIT creance entry posted.
 */
public record PostAdvanceRegularizationJournalEntryCommand(EntityId propertyId, EntityId unitId, LocalDate pieceDate,
                                                             BigDecimal amount, EntityId createdByUserId) {
}
