package com.architek.oikos.party.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PartyJpaRepository extends JpaRepository<PartyEntity, UUID>, JpaSpecificationExecutor<PartyEntity> {

    Optional<PartyEntity> findByPropertyIdAndEmail(UUID propertyId, String email);

    Optional<PartyEntity> findByPropertyIdAndPhone(UUID propertyId, String phone);

    boolean existsByPropertyIdAndEmail(UUID propertyId, String email);

    boolean existsByPropertyIdAndPhone(UUID propertyId, String phone);
}
