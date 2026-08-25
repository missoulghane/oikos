package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.domain.model.RecipientGroup;
import com.architek.oikos.messaging.domain.repository.RecipientGroupRepository;
import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.messaging.infrastructure.mapper.RecipientGroupPersistenceMapper;
import com.architek.oikos.messaging.infrastructure.persistence.RecipientGroupEntity;
import com.architek.oikos.messaging.infrastructure.persistence.RecipientGroupJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class RecipientGroupRepositoryAdapter implements RecipientGroupRepository {

    private final RecipientGroupJpaRepository jpaRepository;
    private final RecipientGroupPersistenceMapper mapper;

    public RecipientGroupRepositoryAdapter(RecipientGroupJpaRepository jpaRepository, RecipientGroupPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /** Find-then-merge, comme les autres adapters : renommer un groupe met à jour sa ligne, il n'en crée pas une seconde. */
    @Override
    public RecipientGroup save(RecipientGroup group) {
        RecipientGroupEntity entity = jpaRepository.findById(group.getId().asUuid()).orElseGet(RecipientGroupEntity::new);
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(group, entity)));
    }

    @Override
    public Optional<RecipientGroup> findById(RecipientGroupId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<RecipientGroup> findAllByPropertyId(EntityId propertyId) {
        return jpaRepository.findByPropertyIdOrderByNameAsc(propertyId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(RecipientGroupId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
