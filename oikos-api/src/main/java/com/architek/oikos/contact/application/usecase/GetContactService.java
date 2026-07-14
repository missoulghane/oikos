package com.architek.oikos.contact.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.port.in.GetContactUseCase;
import com.architek.oikos.contact.application.query.GetContactQuery;
import com.architek.oikos.contact.domain.exception.ContactNotFoundException;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;

@Component
public class GetContactService implements GetContactUseCase {

    private final ContactRepository contactRepository;

    public GetContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ContactView getContact(GetContactQuery query) {
        Contact contact = contactRepository.findById(query.id())
                .orElseThrow(() -> new ContactNotFoundException(query.id()));
        return ContactView.from(contact);
    }
}
