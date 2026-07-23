package com.architek.oikos.contact.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.contact.domain.model.Contact;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.contact.infrastructure.persistence.ContactEntity;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Domain <-> entity mapping. Implemented as default methods rather than
 * auto-generated field mapping since the domain side is composed of value objects
 * (EmailVO, ContactId) that need explicit unwrapping.
 */
@Mapper(componentModel = "spring")
public interface ContactPersistenceMapper {

    default ContactEntity toEntity(Contact contact) {
        return toEntity(contact, new ContactEntity());
    }

    /**
     * Populates an existing (possibly already-managed) entity instance rather than
     * always allocating a new one, so that repository adapters can update in place:
     * a freshly-allocated entity has a null @Version, which Spring Data JPA reads as
     * "new" and would attempt an INSERT instead of an UPDATE for an already-persisted
     * aggregate.
     */
    default ContactEntity toEntity(Contact contact, ContactEntity entity) {
        entity.setId(contact.getId().asUuid());
        entity.setLastName(contact.getLastName());
        entity.setFirstName(contact.getFirstName());
        entity.setEmail(contact.getEmail().value());
        entity.setPhone(contact.getPhone());
        return entity;
    }

    default Contact toDomain(ContactEntity entity) {
        return Contact.reconstruct(
                ContactId.of(entity.getId()),
                entity.getLastName(),
                entity.getFirstName(),
                EmailVO.of(entity.getEmail()),
                entity.getPhone());
    }
}
