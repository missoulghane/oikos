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
class LoadContactIdByPhoneServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Test
    void resolves_the_contact_id_for_a_known_phone_number() {
        ContactId id = ContactId.newId();
        Contact contact = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), "0600000000");
        when(contactRepository.findByPhone("0600000000")).thenReturn(Optional.of(contact));

        var result = new LoadContactIdByPhoneService(contactRepository).loadByPhone("0600000000");

        assertThat(result).contains(id);
    }

    @Test
    void returns_empty_for_an_unknown_phone_number() {
        when(contactRepository.findByPhone("0700000000")).thenReturn(Optional.empty());

        var result = new LoadContactIdByPhoneService(contactRepository).loadByPhone("0700000000");

        assertThat(result).isEmpty();
    }
}
