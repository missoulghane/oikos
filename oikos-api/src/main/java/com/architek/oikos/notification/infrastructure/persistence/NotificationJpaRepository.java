package com.architek.oikos.notification.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByUserIdOrderByCreatedDateDesc(UUID userId, Pageable pageable);

    /** readAt null = jamais ouverte, la même définition que countByUserIdAndReadAtIsNull. */
    Page<NotificationEntity> findByUserIdAndReadAtIsNullOrderByCreatedDateDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(UUID userId);
}
