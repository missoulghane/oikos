package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * ledgerAccountId is the class-6 charge account chosen by the caller (e.g.
 * "Services bancaires") - there is no single functional role for "the"
 * bank-fee expense line. bankAccountId is the caller-chosen BANK account
 * impacted (Partie 3: a property can have several).
 */
public record RecordBankChargeCommand(EntityId propertyId, LocalDate pieceDate, LedgerAccountId ledgerAccountId,
                                       LedgerAccountId bankAccountId, BigDecimal amount, String description,
                                       EntityId createdByUserId) {
}
