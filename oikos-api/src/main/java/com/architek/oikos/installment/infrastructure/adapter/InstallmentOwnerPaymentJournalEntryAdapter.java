package com.architek.oikos.installment.infrastructure.adapter;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.command.PostOwnerPaymentJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.PostOwnerPaymentJournalEntryUseCase;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.installment.application.port.out.OwnerPaymentJournalEntryPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Passes the caller-chosen treasuryAccountId straight through to accounting
 * as a LedgerAccountId (Partie 3) - installment no longer derives CASH/BANK
 * from PaymentMode, the account is picked explicitly and accounting
 * validates it belongs to the property and carries an allowed role.
 */
@Component
public class InstallmentOwnerPaymentJournalEntryAdapter implements OwnerPaymentJournalEntryPort {

    private final PostOwnerPaymentJournalEntryUseCase postOwnerPaymentJournalEntryUseCase;

    public InstallmentOwnerPaymentJournalEntryAdapter(PostOwnerPaymentJournalEntryUseCase postOwnerPaymentJournalEntryUseCase) {
        this.postOwnerPaymentJournalEntryUseCase = postOwnerPaymentJournalEntryUseCase;
    }

    @Override
    public EntityId postOwnerPaymentEntry(EntityId propertyId, EntityId unitId, EntityId treasuryAccountId,
                                           LocalDate pieceDate, BigDecimal imputedAmount, BigDecimal advanceAmount,
                                           String externalReference, EntityId createdByUserId) {
        return postOwnerPaymentJournalEntryUseCase.post(new PostOwnerPaymentJournalEntryCommand(propertyId, unitId,
                new LedgerAccountId(treasuryAccountId), pieceDate, imputedAmount, advanceAmount, externalReference,
                createdByUserId)).value();
    }
}
