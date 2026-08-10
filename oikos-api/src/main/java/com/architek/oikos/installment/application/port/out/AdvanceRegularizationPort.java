package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used by the regularisation use cases to read a unit's
 * un-imputed advance and post the resulting DEBIT avance / CREDIT creance
 * entry, once installment has run the FIFO allocation itself (same
 * imputed/advance split as an owner payment - see PaymentAllocationCalculator).
 * Implemented in installment.infrastructure.adapter by delegating to
 * accounting's public port-in use cases, never to accounting's
 * repository/domain model directly (rule 4/6).
 */
public interface AdvanceRegularizationPort {

    record UnitAdvance(EntityId unitId, BigDecimal amount) {
    }

    /** Net, un-imputed advance available for this unit (never negative). */
    BigDecimal getAvailableAdvance(EntityId propertyId, EntityId unitId);

    /** Every unit of this property carrying a strictly positive, un-imputed advance. */
    List<UnitAdvance> listUnitsWithAvailableAdvance(EntityId propertyId);

    /** Posts the DEBIT avance / CREDIT creance entry, returns the generic EntityId of the
     * resulting JournalEntry (never accounting's own JournalEntryId). */
    EntityId postRegularizationEntry(EntityId propertyId, EntityId unitId, LocalDate pieceDate, BigDecimal amount,
                                      EntityId createdByUserId);
}
