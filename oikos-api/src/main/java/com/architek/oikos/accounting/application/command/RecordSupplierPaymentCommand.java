package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A direct supplier payment: no supplier identity, no accrual step - debit
 * the caller-chosen class-6 charge account (ledgerAccountId, "type de
 * depense"), credit the caller-chosen treasury account (treasuryAccountId,
 * Partie 3 - a property can have several BANK accounts). Same shape as
 * RecordBankChargeCommand; merges what used to be two separate flows
 * (RecordExpenseCommand's accrual + this settlement) since removing the
 * supplier party left the accrual step with no debt to track.
 */
public record RecordSupplierPaymentCommand(EntityId propertyId, LocalDate pieceDate, LedgerAccountId ledgerAccountId,
                                            LedgerAccountId treasuryAccountId, BigDecimal amount, String description,
                                            String externalReference, EntityId createdByUserId) {
}
