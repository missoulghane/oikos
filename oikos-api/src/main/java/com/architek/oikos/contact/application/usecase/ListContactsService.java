package com.architek.oikos.contact.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.port.in.ListContactsUseCase;
import com.architek.oikos.contact.application.query.ListContactsQuery;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListContactsService implements ListContactsUseCase {

    private final ContactRepository contactRepository;

    public ListContactsService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactView> listContacts(ListContactsQuery query) {
        return contactRepository.findAll(query.pageRequest(), query.criteria()).map(ContactView::from);
    }
}
