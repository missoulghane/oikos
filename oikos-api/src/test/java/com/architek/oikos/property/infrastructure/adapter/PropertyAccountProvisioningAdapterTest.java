package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.CreateAccountCommand;
import com.architek.oikos.accounting.application.port.in.CreateAccountUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class PropertyAccountProvisioningAdapterTest {

    @Mock
    private CreateAccountUseCase createAccountUseCase;

    private PropertyAccountProvisioningAdapter newAdapter() {
        return new PropertyAccountProvisioningAdapter(createAccountUseCase);
    }

    @Test
    void provisioning_an_account_creates_a_property_type_account_for_the_holder() {
        EntityId propertyId = EntityId.newId();

        newAdapter().provisionAccount(propertyId);

        ArgumentCaptor<CreateAccountCommand> commandCaptor = ArgumentCaptor.forClass(CreateAccountCommand.class);
        verify(createAccountUseCase).create(commandCaptor.capture());
        assertThat(commandCaptor.getValue().holderId()).isEqualTo(propertyId);
        assertThat(commandCaptor.getValue().accountType()).isEqualTo(AccountType.PROPERTY);
    }
}
