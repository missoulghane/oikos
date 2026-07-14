package com.architek.oikos.contact.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.application.query.ListContactsQuery;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.contact.domain.valueobject.ContactSearchCriteria;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class ListContactsServiceTest {

    @Mock
    private ContactRepository contactRepository;

    private ListContactsService newService() {
        return new ListContactsService(contactRepository);
    }

    @Test
    void listing_contacts_maps_the_repository_page_to_views() {
        Contact contact = Contact.create(ContactId.newId(), "Doe", "Jane", EmailVO.of("jane@doe.com"), null);
        when(contactRepository.findAll(PageRequest.defaultRequest(), ContactSearchCriteria.empty()))
                .thenReturn(Page.of(List.of(contact), 0, 20, 1));

        var query = new ListContactsQuery(PageRequest.defaultRequest(), ContactSearchCriteria.empty());
        var page = newService().listContacts(query);

        assertThat(page.content()).extracting(view -> view.lastName()).containsExactly("Doe");
        assertThat(page.totalElements()).isEqualTo(1);
    }
}
