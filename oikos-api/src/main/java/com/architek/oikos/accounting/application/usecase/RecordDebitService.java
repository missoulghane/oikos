package com.architek.oikos.accounting.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.RecordDebitCommand;
import com.architek.oikos.accounting.application.port.in.RecordDebitUseCase;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;

@Component
public class RecordDebitService implements RecordDebitUseCase {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final AccountBalanceService accountBalanceService;
    private final Clock clock;

    public RecordDebitService(AccountRepository accountRepository, MovementRepository movementRepository,
                              AccountBalanceService accountBalanceService, Clock clock) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.accountBalanceService = accountBalanceService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MovementId record(RecordDebitCommand command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        Movement movement = Movement.create(MovementId.newId(), account.getId(), clock.instant(),
                MovementType.INSTALLMENT, MovementDirection.DEBIT, Amount.of(command.amount()), command.label(), null);
        Movement saved = movementRepository.save(movement);

        accountBalanceService.postMovement(account, saved);

        return saved.getId();
    }
}
