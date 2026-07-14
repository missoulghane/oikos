package com.architek.oikos.contact.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.application.query.GetContactQuery;
import com.architek.oikos.contact.domain.exception.ContactNotFoundException;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class GetContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    private GetContactService newService() {
        return new GetContactService(contactRepository);
    }

    @Test
    void getting_an_existing_contact_returns_its_view() {
        ContactId id = ContactId.newId();
        Contact contact = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));

        var view = newService().getContact(new GetContactQuery(id));

        assertThat(view.lastName()).isEqualTo("Doe");
        assertThat(view.email()).isEqualTo("jane@doe.com");
    }

    @Test
    void getting_a_missing_contact_throws() {
        ContactId id = ContactId.newId();
        when(contactRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getContact(new GetContactQuery(id)))
                .isInstanceOf(ContactNotFoundException.class);
    }
}
