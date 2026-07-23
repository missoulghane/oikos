package com.architek.oikos.contact.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.application.command.UpdateContactCommand;
import com.architek.oikos.contact.domain.exception.ContactNotFoundException;
import com.architek.oikos.contact.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class UpdateContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    private UpdateContactService newService() {
        return new UpdateContactService(contactRepository);
    }

    @Test
    void updating_a_contact_persists_the_new_fields() {
        ContactId id = ContactId.newId();
        Contact contact = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdateContactCommand(id, "Smith", "Janet", EmailVO.of("janet@smith.com"), "0700000000");
        var view = newService().update(command);

        assertThat(view.lastName()).isEqualTo("Smith");
        assertThat(view.email()).isEqualTo("janet@smith.com");
    }

    @Test
    void updating_with_the_same_email_does_not_trigger_a_uniqueness_check() {
        ContactId id = ContactId.newId();
        Contact contact = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdateContactCommand(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), "0700000000");

        newService().update(command);
    }

    @Test
    void updating_to_an_email_already_used_by_another_contact_is_rejected() {
        ContactId id = ContactId.newId();
        Contact contact = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));
        when(contactRepository.existsByEmail(EmailVO.of("taken@doe.com"))).thenReturn(true);

        var command = new UpdateContactCommand(id, "Doe", "Jane", EmailVO.of("taken@doe.com"), null);

        assertThatThrownBy(() -> newService().update(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void updating_a_missing_contact_throws() {
        ContactId id = ContactId.newId();
        when(contactRepository.findById(id)).thenReturn(Optional.empty());

        var command = new UpdateContactCommand(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);

        assertThatThrownBy(() -> newService().update(command)).isInstanceOf(ContactNotFoundException.class);
    }
}
