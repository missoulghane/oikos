package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.repository.MessageDraftRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.messaging.infrastructure.mapper.MessageDraftPersistenceMapper;
import com.architek.oikos.messaging.infrastructure.persistence.MessageDraftEntity;
import com.architek.oikos.messaging.infrastructure.persistence.MessageDraftJpaRepository;
import com.architek.oikos.messaging.infrastructure.persistence.MessageDraftSpecifications;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class MessageDraftRepositoryAdapter implements MessageDraftRepository {

    private final MessageDraftJpaRepository jpaRepository;
    private final MessageDraftPersistenceMapper mapper;

    public MessageDraftRepositoryAdapter(MessageDraftJpaRepository jpaRepository, MessageDraftPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MessageDraft save(MessageDraft draft) {
        MessageDraftEntity entity = jpaRepository.findById(draft.getId().asUuid()).orElseGet(MessageDraftEntity::new);
        MessageDraftEntity saved = jpaRepository.save(mapper.toEntity(draft, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MessageDraft> findById(MessageDraftId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<MessageDraft> findByCreatedBy(EntityId createdBy, PageRequest pageRequest, String search) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        Specification<MessageDraftEntity> specification = MessageDraftSpecifications.matching(createdBy.value(), search);
        org.springframework.data.domain.Page<MessageDraftEntity> springPage = jpaRepository.findAll(specification, pageable);
        List<MessageDraft> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public void deleteById(MessageDraftId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
