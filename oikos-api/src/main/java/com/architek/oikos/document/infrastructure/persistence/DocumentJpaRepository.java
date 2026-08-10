package com.architek.oikos.document.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, UUID> {

    Page<DocumentEntity> findByOwnerTypeAndOwnerId(String ownerType, UUID ownerId, Pageable pageable);

    boolean existsByOwnerTypeAndOwnerIdAndChecksumSha256(String ownerType, UUID ownerId, String checksumSha256);
}
