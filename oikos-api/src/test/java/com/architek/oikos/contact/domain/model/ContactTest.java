package com.architek.oikos.contact.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

class ContactTest {

    private static Contact newContact() {
        return Contact.create(ContactId.newId(), "Doe", "Jane", EmailVO.of("jane@doe.com"), "0600000000");
    }

    @Test
    void create_builds_a_contact_with_the_given_fields() {
        Contact contact = newContact();

        assertThat(contact.getLastName()).isEqualTo("Doe");
        assertThat(contact.getFirstName()).isEqualTo("Jane");
        assertThat(contact.getEmail()).isEqualTo(EmailVO.of("jane@doe.com"));
        assertThat(contact.getPhone()).isEqualTo("0600000000");
    }

    @Test
    void create_allows_a_null_phone() {
        Contact contact = Contact.create(ContactId.newId(), "Doe", "Jane", EmailVO.of("jane@doe.com"), null);

        assertThat(contact.getPhone()).isNull();
    }

    @Test
    void create_rejects_a_blank_last_name() {
        assertThatThrownBy(() -> Contact.create(ContactId.newId(), " ", "Jane", EmailVO.of("jane@doe.com"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withContactInfo_returns_a_new_instance_with_updated_fields_and_same_identity() {
        Contact contact = newContact();

        Contact updated = contact.withContactInfo("Smith", "Janet", EmailVO.of("janet@smith.com"), "0700000000");

        assertThat(updated.getLastName()).isEqualTo("Smith");
        assertThat(updated.getFirstName()).isEqualTo("Janet");
        assertThat(updated.getEmail()).isEqualTo(EmailVO.of("janet@smith.com"));
        assertThat(updated.getPhone()).isEqualTo("0700000000");
        assertThat(updated).isEqualTo(contact);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        ContactId id = ContactId.newId();
        Contact a = Contact.create(id, "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        Contact b = Contact.create(id, "Smith", "Janet", EmailVO.of("janet@smith.com"), "0700000000");

        assertThat(a).isEqualTo(b);
    }
}
