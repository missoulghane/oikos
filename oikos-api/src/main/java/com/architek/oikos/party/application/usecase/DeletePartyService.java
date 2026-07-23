package com.architek.oikos.contact.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.contact.application.command.DeleteContactCommand;
import com.architek.oikos.contact.application.port.in.DeleteContactUseCase;
import com.architek.oikos.contact.domain.exception.ContactNotFoundException;
import com.architek.oikos.contact.domain.repository.ContactRepository;

@Component
public class DeleteContactService implements DeleteContactUseCase {

    private final ContactRepository contactRepository;

    public DeleteContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteContactCommand command) {
        if (contactRepository.findById(command.id()).isEmpty()) {
            throw new ContactNotFoundException(command.id());
        }
        contactRepository.deleteById(command.id());
    }
}
