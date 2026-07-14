package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.infrastructure.persistence.UnitOwnershipEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface UnitOwnershipPersistenceMapper {

    default UnitOwnershipEntity toEntity(UnitOwnership unitOwnership) {
        return toEntity(unitOwnership, new UnitOwnershipEntity());
    }

    default UnitOwnershipEntity toEntity(UnitOwnership unitOwnership, UnitOwnershipEntity entity) {
        entity.setId(unitOwnership.getId().asUuid());
        entity.setUnitId(unitOwnership.getUnitId().asUuid());
        entity.setContactId(unitOwnership.getContactId().value());
        entity.setOwnershipShare(unitOwnership.getOwnershipShare().value());
        return entity;
    }

    default UnitOwnership toDomain(UnitOwnershipEntity entity) {
        return UnitOwnership.reconstruct(UnitOwnershipId.of(entity.getId()), UnitId.of(entity.getUnitId()),
                EntityId.of(entity.getContactId()), OwnershipShare.of(entity.getOwnershipShare()));
    }
}
