package com.architek.oikos.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DevicePushTokenJpaRepository extends JpaRepository<DevicePushTokenEntity, UUID> {

    Optional<DevicePushTokenEntity> findByExpoPushToken(String expoPushToken);

    List<DevicePushTokenEntity> findByUserId(UUID userId);
}
