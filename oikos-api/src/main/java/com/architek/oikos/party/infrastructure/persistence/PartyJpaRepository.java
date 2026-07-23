package com.architek.oikos.party.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PartyJpaRepository extends JpaRepository<PartyEntity, UUID>, JpaSpecificationExecutor<PartyEntity> {

    Optional<PartyEntity> findByEmail(String email);

    Optional<PartyEntity> findByPhone(String phone);

    boolean existsByEmail(String email);
}
