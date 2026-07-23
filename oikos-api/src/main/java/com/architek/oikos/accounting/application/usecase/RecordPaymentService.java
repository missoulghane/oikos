package com.architek.oikos.accounting.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.RecordPaymentCommand;
import com.architek.oikos.accounting.application.port.in.RecordPaymentUseCase;
import com.architek.oikos.accounting.application.port.out.AutoAllocationPort;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;

/**
 * RG004: a payment always creates a credit movement. FIFO auto-allocation is
 * then attempted against any due installments (no allocation is required to
 * succeed - RG007: an unallocated amount simply remains available credit).
 */
@Component
public class RecordPaymentService implements RecordPaymentUseCase {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final AutoAllocationPort autoAllocationPort;
    private final AccountBalanceService accountBalanceService;
    private final Clock clock;

    public RecordPaymentService(AccountRepository accountRepository, MovementRepository movementRepository,
                                 AutoAllocationPort autoAllocationPort, AccountBalanceService accountBalanceService,
                                 Clock clock) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.autoAllocationPort = autoAllocationPort;
        this.accountBalanceService = accountBalanceService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MovementId record(RecordPaymentCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        Movement movement = Movement.create(MovementId.newId(), command.accountId(), clock.instant(),
                MovementType.PAYMENT, MovementDirection.CREDIT, Amount.of(command.amount()), command.label(),
                command.businessReference());
        Movement saved = movementRepository.save(movement);

        accountBalanceService.postMovement(account, saved);
        autoAllocationPort.allocate(command.accountId());

        return saved.getId();
    }
}
