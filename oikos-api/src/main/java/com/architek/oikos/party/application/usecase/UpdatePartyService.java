package com.architek.oikos.contact.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.contact.application.command.UpdateContactCommand;
import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.port.in.UpdateContactUseCase;
import com.architek.oikos.contact.domain.exception.ContactNotFoundException;
import com.architek.oikos.contact.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;

@Component
public class UpdateContactService implements UpdateContactUseCase {

    private final ContactRepository contactRepository;

    public UpdateContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional
    public ContactView update(UpdateContactCommand command) {
        Contact contact = contactRepository.findById(command.id())
                .orElseThrow(() -> new ContactNotFoundException(command.id()));
        if (!contact.getEmail().equals(command.email()) && contactRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        Contact updated = contactRepository.save(
                contact.withContactInfo(command.lastName(), command.firstName(), command.email(), command.phone()));
        return ContactView.from(updated);
    }
}
