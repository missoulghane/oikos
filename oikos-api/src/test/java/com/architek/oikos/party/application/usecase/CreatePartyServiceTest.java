package com.architek.oikos.contact.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.application.command.CreateContactCommand;
import com.architek.oikos.contact.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class CreateContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    private CreateContactService newService() {
        return new CreateContactService(contactRepository);
    }

    @Test
    void creating_a_contact_persists_it() {
        when(contactRepository.existsByEmail(any())).thenReturn(false);
        when(contactRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateContactCommand command = new CreateContactCommand("Doe", "Jane", EmailVO.of("jane@doe.com"), null);

        newService().create(command);

        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).save(captor.capture());
        assertThat(captor.getValue().getLastName()).isEqualTo("Doe");
        assertThat(captor.getValue().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void creating_a_contact_with_an_already_used_email_is_rejected() {
        when(contactRepository.existsByEmail(any())).thenReturn(true);

        CreateContactCommand command = new CreateContactCommand("Doe", "Jane", EmailVO.of("jane@doe.com"), null);

        assertThatThrownBy(() -> newService().create(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }
}
