package com.architek.oikos.notification.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.notification.domain.model.DevicePushToken;
import com.architek.oikos.notification.domain.repository.DevicePushTokenRepository;
import com.architek.oikos.notification.domain.valueobject.DevicePushTokenId;
import com.architek.oikos.notification.infrastructure.persistence.DevicePushTokenEntity;
import com.architek.oikos.notification.infrastructure.persistence.DevicePushTokenJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class DevicePushTokenRepositoryAdapter implements DevicePushTokenRepository {

    private final DevicePushTokenJpaRepository jpaRepository;

    public DevicePushTokenRepositoryAdapter(DevicePushTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DevicePushToken save(DevicePushToken devicePushToken) {
        // Upsert keyed on the token itself, not on devicePushToken.id() (which
        // DevicePushToken.register() mints fresh every call, unlike
        // RefreshToken's stable id across saves) - only assign the freshly
        // generated id to a genuinely new row, so re-registering an existing
        // token updates that row in place instead of moving its primary key.
        DevicePushTokenEntity entity = jpaRepository.findByExpoPushToken(devicePushToken.expoPushToken())
                .orElseGet(DevicePushTokenEntity::new);
        if (entity.getId() == null) {
            entity.setId(devicePushToken.id().asUuid());
        }
        entity.setUserId(devicePushToken.userId().value());
        entity.setExpoPushToken(devicePushToken.expoPushToken());
        DevicePushTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<String> findTokensByUserId(EntityId userId) {
        return jpaRepository.findByUserId(userId.value()).stream().map(DevicePushTokenEntity::getExpoPushToken).toList();
    }

    private DevicePushToken toDomain(DevicePushTokenEntity entity) {
        return new DevicePushToken(DevicePushTokenId.of(entity.getId()), EntityId.of(entity.getUserId()), entity.getExpoPushToken());
    }
}
