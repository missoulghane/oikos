package com.architek.oikos.contact.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.contact.application.command.CreateContactCommand;
import com.architek.oikos.contact.application.port.in.CreateContactUseCase;
import com.architek.oikos.contact.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;

@Component
public class CreateContactService implements CreateContactUseCase {

    private final ContactRepository contactRepository;

    public CreateContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional
    public ContactId create(CreateContactCommand command) {
        if (contactRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        Contact contact = Contact.create(ContactId.newId(), command.lastName(), command.firstName(),
                command.email(), command.phone());
        return contactRepository.save(contact).getId();
    }
}
