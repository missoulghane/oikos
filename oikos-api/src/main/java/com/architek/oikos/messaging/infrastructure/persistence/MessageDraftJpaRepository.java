package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessageDraftJpaRepository extends JpaRepository<MessageDraftEntity, UUID>,
        JpaSpecificationExecutor<MessageDraftEntity> {
}
