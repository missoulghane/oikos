package com.architek.oikos.contact.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.contact.domain.valueobject.ContactSearchCriteria;
import com.architek.oikos.contact.infrastructure.mapper.ContactPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ContactRepositoryAdapter.class, ContactPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class ContactRepositoryAdapterDataJpaTest {

    @Autowired
    private ContactRepositoryAdapter adapter;

    private static Contact newContact() {
        return Contact.create(ContactId.newId(), "Doe", "Jane", EmailVO.of("jpa-test@oikos.com"), "0600000000");
    }

    @Test
    void saves_and_finds_a_contact_by_id() {
        Contact contact = newContact();

        adapter.save(contact);

        assertThat(adapter.findById(contact.getId())).isPresent()
                .get().extracting(Contact::getLastName).isEqualTo("Doe");
    }

    @Test
    void existsByEmail_reflects_persisted_state() {
        assertThat(adapter.existsByEmail(EmailVO.of("nobody@oikos.com"))).isFalse();

        adapter.save(Contact.create(ContactId.newId(), "A", "B", EmailVO.of("nobody@oikos.com"), null));

        assertThat(adapter.existsByEmail(EmailVO.of("nobody@oikos.com"))).isTrue();
    }

    @Test
    void findByEmail_returns_the_matching_contact() {
        Contact contact = newContact();
        adapter.save(contact);

        assertThat(adapter.findByEmail(EmailVO.of("jpa-test@oikos.com"))).isPresent()
                .get().extracting(Contact::getId).isEqualTo(contact.getId());
        assertThat(adapter.findByEmail(EmailVO.of("nobody@oikos.com"))).isEmpty();
    }

    @Test
    void findByPhone_returns_the_matching_contact() {
        Contact contact = newContact();
        adapter.save(contact);

        assertThat(adapter.findByPhone("0600000000")).isPresent()
                .get().extracting(Contact::getId).isEqualTo(contact.getId());
        assertThat(adapter.findByPhone("0000000000")).isEmpty();
    }

    @Test
    void updating_a_contact_persists_changes_without_creating_a_duplicate() {
        Contact contact = newContact();
        adapter.save(contact);

        adapter.save(contact.withContactInfo("Smith", "Janet", contact.getEmail(), "0700000000"));

        Contact reloaded = adapter.findById(contact.getId()).orElseThrow();
        assertThat(reloaded.getLastName()).isEqualTo("Smith");
        assertThat(reloaded.getPhone()).isEqualTo("0700000000");
    }

    @Test
    void findAll_paginates_results() {
        for (int i = 0; i < 3; i++) {
            adapter.save(Contact.create(ContactId.newId(), "Doe" + i, "U", EmailVO.of("user" + i + "@oikos.com"), null));
        }

        var page = adapter.findAll(PageRequest.of(0, 2), ContactSearchCriteria.empty());

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void findAll_filters_by_search_text() {
        adapter.save(Contact.create(ContactId.newId(), "Martin", "Alice", EmailVO.of("alice@oikos.com"), null));
        adapter.save(Contact.create(ContactId.newId(), "Durand", "Bob", EmailVO.of("bob@oikos.com"), null));

        var page = adapter.findAll(PageRequest.of(0, 20), new ContactSearchCriteria("alice"));

        assertThat(page.content()).extracting(Contact::getFirstName).containsExactly("Alice");
    }

    @Test
    void deleteById_removes_the_contact() {
        Contact contact = newContact();
        adapter.save(contact);

        adapter.deleteById(contact.getId());

        assertThat(adapter.findById(contact.getId())).isEmpty();
    }
}
