package com.architek.oikos.contact.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class LoadContactIdByEmailServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Test
    void resolves_the_contact_id_for_a_known_email() {
        ContactId id = ContactId.newId();
        Contact contact = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        when(contactRepository.findByEmail(EmailVO.of("jane@doe.com"))).thenReturn(Optional.of(contact));

        var result = new LoadContactIdByEmailService(contactRepository).loadByEmail(EmailVO.of("jane@doe.com"));

        assertThat(result).contains(id);
    }

    @Test
    void returns_empty_for_an_unknown_email() {
        when(contactRepository.findByEmail(EmailVO.of("nobody@doe.com"))).thenReturn(Optional.empty());

        var result = new LoadContactIdByEmailService(contactRepository).loadByEmail(EmailVO.of("nobody@doe.com"));

        assertThat(result).isEmpty();
    }
}
