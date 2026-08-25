package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientGroupJpaRepository extends JpaRepository<RecipientGroupEntity, UUID> {

    List<RecipientGroupEntity> findByPropertyIdOrderByNameAsc(UUID propertyId);
}
