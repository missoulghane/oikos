package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A virement interne (spec: mouvement entre comptes de tresorerie) - moves
 * funds between two of the property's own CASH/BANK accounts (e.g. a
 * deposit from caisse to banque, or a withdrawal from banque to caisse).
 * Both accounts are caller-chosen and resolved/validated by
 * TreasuryAccountResolver, same as every other treasury-touching use case.
 */
public record RecordTreasuryTransferCommand(EntityId propertyId, LedgerAccountId sourceAccountId,
                                              LedgerAccountId destinationAccountId, LocalDate pieceDate,
                                              BigDecimal amount, String description, EntityId createdByUserId) {
}
