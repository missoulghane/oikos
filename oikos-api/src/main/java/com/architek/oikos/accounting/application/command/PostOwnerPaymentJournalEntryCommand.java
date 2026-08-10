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
 * at post() time); either one may be zero but not both. cashPayment is
 * installment's PaymentMode.CASH collapsed to a boolean here (accounting
 * must not depend on installment's domain types) - checked against the
 * resolved account's role (Moroccan cash-accounting rule: a caisse account
 * only ever receives especes, a banque account never does) once the account
 * is already resolved, rather than adding a separate role-lookup port.
 */
public record PostOwnerPaymentJournalEntryCommand(EntityId propertyId, EntityId unitId,
                                                    LedgerAccountId treasuryAccountId, LocalDate pieceDate,
                                                    BigDecimal imputedAmount, BigDecimal advanceAmount,
                                                    String externalReference, EntityId createdByUserId,
                                                    boolean cashPayment) {
}
