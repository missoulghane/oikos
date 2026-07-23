package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.out.UnitDirectoryPort;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Keeps Account.balance persisted in sync with every recorded Movement, and
 * mirrors unit-side movements onto their property's account: property
 * bookkeeping is the inverse of unit bookkeeping (a debit on a unit implies a
 * credit on its property, and vice versa), applied to every movement -
 * payments included, not just installment-call debits. Not exposed as its own
 * port-in: an internal collaborator invoked by RecordPaymentService and
 * RecordInstallmentCallService right after they save a Movement.
 */
@Component
class AccountBalanceService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final UnitDirectoryPort unitDirectoryPort;

    AccountBalanceService(AccountRepository accountRepository, MovementRepository movementRepository,
                           UnitDirectoryPort unitDirectoryPort) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.unitDirectoryPort = unitDirectoryPort;
    }

    void postMovement(Account account, Movement movement) {
        accountRepository.save(account.applyMovement(movement.getDirection(), movement.getAmount().value()));

        if (account.getAccountType() != AccountType.UNIT) {
            return;
        }

        EntityId propertyId = unitDirectoryPort.resolvePropertyId(account.getHolderId());
        Account propertyAccount = accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)
                .orElseThrow(() -> AccountNotFoundException.forHolder(propertyId, AccountType.PROPERTY));

        MovementDirection mirroredDirection = movement.getDirection() == MovementDirection.CREDIT
                ? MovementDirection.DEBIT : MovementDirection.CREDIT;
        Movement mirrored = Movement.create(MovementId.newId(), propertyAccount.getId(), movement.getOccurredOn(),
                movement.getType(), mirroredDirection, movement.getAmount(), movement.getLabel(), movement.getBusinessReference());
        movementRepository.save(mirrored);

        accountRepository.save(propertyAccount.applyMovement(mirroredDirection, movement.getAmount().value()));
    }
}
