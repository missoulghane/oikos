package com.architek.oikos.contact.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.repository.ContactRepository;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.contact.domain.valueobject.ContactSearchCriteria;
import com.architek.oikos.contact.infrastructure.mapper.ContactPersistenceMapper;
import com.architek.oikos.contact.infrastructure.persistence.ContactEntity;
import com.architek.oikos.contact.infrastructure.persistence.ContactJpaRepository;
import com.architek.oikos.contact.infrastructure.persistence.ContactSpecifications;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@Component
public class ContactRepositoryAdapter implements ContactRepository {

    private final ContactJpaRepository jpaRepository;
    private final ContactPersistenceMapper mapper;

    public ContactRepositoryAdapter(ContactJpaRepository jpaRepository, ContactPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Contact save(Contact contact) {
        ContactEntity entity = jpaRepository.findById(contact.getId().asUuid()).orElseGet(ContactEntity::new);
        ContactEntity saved = jpaRepository.save(mapper.toEntity(contact, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Contact> findById(ContactId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<Contact> findByEmail(EmailVO email) {
        return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Contact> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(EmailVO email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public Page<Contact> findAll(PageRequest pageRequest, ContactSearchCriteria criteria) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        Specification<ContactEntity> specification = ContactSpecifications.matching(criteria);
        org.springframework.data.domain.Page<ContactEntity> springPage = jpaRepository.findAll(specification, pageable);
        List<Contact> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public void deleteById(ContactId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
