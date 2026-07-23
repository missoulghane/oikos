package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.CreateAccountCommand;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.application.port.out.UnitDirectoryPort;
import com.architek.oikos.accounting.domain.exception.DuplicateAccountException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.exception.UnitNotFoundException;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    private CreateAccountService newService() {
        return new CreateAccountService(accountRepository, unitDirectoryPort, propertyDirectoryPort);
    }

    @Test
    void creating_a_unit_account_for_a_unit_without_one_succeeds() {
        EntityId unitId = EntityId.newId();
        when(unitDirectoryPort.exists(unitId)).thenReturn(true);
        when(accountRepository.existsByHolderId(unitId, AccountType.UNIT)).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateAccountCommand(unitId, AccountType.UNIT));
    }

    @Test
    void creating_a_property_account_for_a_property_without_one_succeeds() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(accountRepository.existsByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateAccountCommand(propertyId, AccountType.PROPERTY));
    }

    @Test
    void creating_a_second_account_for_the_same_unit_is_rejected() {
        EntityId unitId = EntityId.newId();
        when(unitDirectoryPort.exists(unitId)).thenReturn(true);
        when(accountRepository.existsByHolderId(unitId, AccountType.UNIT)).thenReturn(true);

        assertThatThrownBy(() -> newService().create(new CreateAccountCommand(unitId, AccountType.UNIT)))
                .isInstanceOf(DuplicateAccountException.class);
    }

    @Test
    void creating_an_account_for_an_unknown_unit_is_rejected() {
        EntityId unitId = EntityId.newId();
        when(unitDirectoryPort.exists(unitId)).thenReturn(false);

        assertThatThrownBy(() -> newService().create(new CreateAccountCommand(unitId, AccountType.UNIT)))
                .isInstanceOf(UnitNotFoundException.class);
    }

    @Test
    void creating_an_account_for_an_unknown_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        assertThatThrownBy(() -> newService().create(new CreateAccountCommand(propertyId, AccountType.PROPERTY)))
                .isInstanceOf(PropertyNotFoundException.class);
    }
}
