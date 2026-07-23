package com.architek.oikos.contact.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.contact.application.port.in.LoadContactIdByEmailUseCase;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@Component
public class LoadContactIdByEmailService implements LoadContactIdByEmailUseCase {

    private final ContactRepository contactRepository;

    public LoadContactIdByEmailService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactId> loadByEmail(EmailVO email) {
        return contactRepository.findByEmail(email).map(contact -> contact.getId());
    }
}
