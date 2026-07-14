package com.architek.oikos.contact.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContactJpaRepository extends JpaRepository<ContactEntity, UUID>, JpaSpecificationExecutor<ContactEntity> {

    Optional<ContactEntity> findByEmail(String email);

    Optional<ContactEntity> findByPhone(String phone);

    boolean existsByEmail(String email);
}
