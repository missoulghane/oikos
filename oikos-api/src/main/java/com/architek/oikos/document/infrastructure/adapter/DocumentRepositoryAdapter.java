package com.architek.oikos.document.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.repository.DocumentRepository;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.document.infrastructure.mapper.DocumentPersistenceMapper;
import com.architek.oikos.document.infrastructure.persistence.DocumentEntity;
import com.architek.oikos.document.infrastructure.persistence.DocumentJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class DocumentRepositoryAdapter implements DocumentRepository {

    private final DocumentJpaRepository jpaRepository;
    private final DocumentPersistenceMapper mapper;

    public DocumentRepositoryAdapter(DocumentJpaRepository jpaRepository, DocumentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Document save(Document document) {
        DocumentEntity entity = jpaRepository.findById(document.getId().asUuid()).orElseGet(DocumentEntity::new);
        DocumentEntity saved = jpaRepository.save(mapper.toEntity(document, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Document> findById(DocumentId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<Document> findAllByOwner(DocumentOwnerType ownerType, EntityId ownerId, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<DocumentEntity> springPage =
                jpaRepository.findByOwnerTypeAndOwnerId(ownerType.name(), ownerId.value(), pageable);
        List<Document> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public void deleteById(DocumentId id) {
        jpaRepository.deleteById(id.asUuid());
    }

    @Override
    public boolean existsByOwnerAndChecksum(DocumentOwnerType ownerType, EntityId ownerId, String checksumSha256) {
        return jpaRepository.existsByOwnerTypeAndOwnerIdAndChecksumSha256(ownerType.name(), ownerId.value(),
                checksumSha256);
    }
}
