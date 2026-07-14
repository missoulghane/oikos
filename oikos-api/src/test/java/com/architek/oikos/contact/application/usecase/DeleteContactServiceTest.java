package com.architek.oikos.contact.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.application.command.DeleteContactCommand;
import com.architek.oikos.contact.domain.exception.ContactNotFoundException;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class DeleteContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    private DeleteContactService newService() {
        return new DeleteContactService(contactRepository);
    }

    @Test
    void deleting_an_existing_contact_removes_it() {
        ContactId id = ContactId.newId();
        Contact contact = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));

        newService().delete(new DeleteContactCommand(id));

        verify(contactRepository).deleteById(id);
    }

    @Test
    void deleting_a_missing_contact_throws() {
        ContactId id = ContactId.newId();
        when(contactRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().delete(new DeleteContactCommand(id)))
                .isInstanceOf(ContactNotFoundException.class);
    }
}
