package com.architek.oikos.notification.infrastructure.adapter;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.repository.NotificationRepository;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.notification.infrastructure.mapper.NotificationPersistenceMapper;
import com.architek.oikos.notification.infrastructure.persistence.NotificationEntity;
import com.architek.oikos.notification.infrastructure.persistence.NotificationJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationPersistenceMapper mapper;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository, NotificationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = jpaRepository.findById(notification.getId().asUuid()).orElseGet(NotificationEntity::new);
        NotificationEntity saved = jpaRepository.save(mapper.toEntity(notification, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<Notification> findByRecipientUserId(EntityId recipientUserId, boolean unreadOnly, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<NotificationEntity> springPage = unreadOnly
                ? jpaRepository.findByUserIdAndReadAtIsNullOrderByCreatedDateDesc(recipientUserId.value(), pageable)
                : jpaRepository.findByUserIdOrderByCreatedDateDesc(recipientUserId.value(), pageable);
        var content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public long countUnreadByRecipientUserId(EntityId recipientUserId) {
        return jpaRepository.countByUserIdAndReadAtIsNull(recipientUserId.value());
    }
}
