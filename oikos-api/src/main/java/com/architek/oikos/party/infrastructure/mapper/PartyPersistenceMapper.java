package com.architek.oikos.party.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.infrastructure.persistence.PartyEntity;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Domain <-> entity mapping. Implemented as default methods rather than
 * auto-generated field mapping since the domain side is composed of value objects
 * (EmailVO, PartyId) that need explicit unwrapping.
 */
@Mapper(componentModel = "spring")
public interface PartyPersistenceMapper {

    default PartyEntity toEntity(Party party) {
        return toEntity(party, new PartyEntity());
    }

    /**
     * Populates an existing (possibly already-managed) entity instance rather than
     * always allocating a new one, so that repository adapters can update in place:
     * a freshly-allocated entity has a null @Version, which Spring Data JPA reads as
     * "new" and would attempt an INSERT instead of an UPDATE for an already-persisted
     * aggregate.
     */
    default PartyEntity toEntity(Party party, PartyEntity entity) {
        entity.setId(party.getId().asUuid());
        entity.setPropertyId(party.getPropertyId().value());
        entity.setFullName(party.getFullName());
        entity.setPartyType(party.getPartyType());
        entity.setEmail(party.getEmail().map(EmailVO::value).orElse(null));
        entity.setPhone(party.getPhone());
        return entity;
    }

    default Party toDomain(PartyEntity entity) {
        return Party.reconstruct(
                PartyId.of(entity.getId()),
                EntityId.of(entity.getPropertyId()),
                entity.getFullName(),
                entity.getPartyType(),
                entity.getEmail() != null ? EmailVO.of(entity.getEmail()) : null,
                entity.getPhone());
    }
}
