package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * treasuryAccountId is the caller-chosen CASH or BANK account impacted
 * (Partie 3: a property can have several BANK accounts, so the role alone no
 * longer identifies a single account) - resolved and validated by
 * TreasuryAccountResolver. imputedAmount + advanceAmount must equal the
 * amount actually received (enforced by the caller via
 * PaymentAllocationCalculator, not re-checked here beyond I1's balance check
 * at post() time); either one may be zero but not both.
 */
public record PostOwnerPaymentJournalEntryCommand(EntityId propertyId, EntityId unitId,
                                                    LedgerAccountId treasuryAccountId, LocalDate pieceDate,
                                                    BigDecimal imputedAmount, BigDecimal advanceAmount,
                                                    String externalReference, EntityId createdByUserId) {
}
