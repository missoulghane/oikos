package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used by RecordOwnerPaymentUseCase to post the treasury
 * journal entry (BQ or CA) for an owner payment (P2/P3, spec &sect;4.1/
 * &sect;4.2) once the FIFO imputation/advance split is known. Implemented in
 * installment.infrastructure.adapter by delegating to accounting's public
 * port-in (PostOwnerPaymentJournalEntryUseCase), never to accounting's
 * repository/domain model directly (rule 4/6). Returns the generic EntityId
 * of the resulting JournalEntry, never accounting's own JournalEntryId.
 * treasuryAccountId is the caller-chosen CASH/BANK account impacted (Partie
 * 3: a property can have several BANK accounts) - accounting validates it.
 */
public interface OwnerPaymentJournalEntryPort {

    EntityId postOwnerPaymentEntry(EntityId propertyId, EntityId unitId, EntityId treasuryAccountId,
                                    LocalDate pieceDate, BigDecimal imputedAmount, BigDecimal advanceAmount,
                                    String externalReference, EntityId createdByUserId);
}
