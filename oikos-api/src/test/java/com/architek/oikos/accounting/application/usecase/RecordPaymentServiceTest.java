package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.RecordPaymentCommand;
import com.architek.oikos.accounting.application.port.out.AutoAllocationPort;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordPaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private AutoAllocationPort autoAllocationPort;

    @Mock
    private AccountBalanceService accountBalanceService;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2027-01-01T00:00:00Z"), ZoneOffset.UTC);

    private RecordPaymentService newService() {
        return new RecordPaymentService(accountRepository, movementRepository, autoAllocationPort, accountBalanceService, FIXED_CLOCK);
    }

    @Test
    void recording_a_payment_creates_a_credit_movement_and_triggers_auto_allocation() {
        AccountId accountId = AccountId.newId();
        Account account = Account.create(accountId, EntityId.newId(), AccountType.UNIT);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(movementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().record(new RecordPaymentCommand(accountId, new BigDecimal("500"), "Paiement", null));

        ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(captor.capture());
        Movement saved = captor.getValue();
        assertThat(saved.getAccountId()).isEqualTo(accountId);
        assertThat(saved.getType()).isEqualTo(MovementType.PAYMENT);
        assertThat(saved.getDirection()).isEqualTo(MovementDirection.CREDIT);
        assertThat(saved.getAmount().value()).isEqualByComparingTo("500");
        assertThat(saved.getOccurredOn()).isEqualTo(FIXED_CLOCK.instant());

        verify(accountBalanceService).postMovement(account, saved);
        verify(autoAllocationPort).allocate(accountId);
    }

    @Test
    void recording_a_payment_on_an_unknown_account_is_rejected() {
        AccountId accountId = AccountId.newId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().record(new RecordPaymentCommand(accountId, BigDecimal.TEN, "Paiement", null)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
