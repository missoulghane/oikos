package com.architek.oikos.document.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, UUID>,
        JpaSpecificationExecutor<DocumentEntity> {

    boolean existsByOwnerTypeAndOwnerIdAndChecksumSha256(String ownerType, UUID ownerId, String checksumSha256);
}
