package com.architek.oikos.contact.domain.repository;

import java.util.Optional;

import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.contact.domain.valueobject.ContactSearchCriteria;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

public interface ContactRepository {

    Contact save(Contact contact);

    Optional<Contact> findById(ContactId id);

    Optional<Contact> findByEmail(EmailVO email);

    Optional<Contact> findByPhone(String phone);

    boolean existsByEmail(EmailVO email);

    Page<Contact> findAll(PageRequest pageRequest, ContactSearchCriteria criteria);

    void deleteById(ContactId id);
}
