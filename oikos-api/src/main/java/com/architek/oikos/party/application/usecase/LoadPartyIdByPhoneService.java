package com.architek.oikos.contact.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.contact.application.port.in.LoadContactIdByPhoneUseCase;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;

@Component
public class LoadContactIdByPhoneService implements LoadContactIdByPhoneUseCase {

    private final ContactRepository contactRepository;

    public LoadContactIdByPhoneService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactId> loadByPhone(String phone) {
        return contactRepository.findByPhone(phone).map(contact -> contact.getId());
    }
}
