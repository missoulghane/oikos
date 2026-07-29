package com.architek.oikos.party.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.party.infrastructure.mapper.PartyPersistenceMapper;
import com.architek.oikos.party.infrastructure.persistence.PartyEntity;
import com.architek.oikos.party.infrastructure.persistence.PartyJpaRepository;
import com.architek.oikos.party.infrastructure.persistence.PartySpecifications;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class PartyRepositoryAdapter implements PartyRepository {

    private final PartyJpaRepository jpaRepository;
    private final PartyPersistenceMapper mapper;

    public PartyRepositoryAdapter(PartyJpaRepository jpaRepository, PartyPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Party save(Party party) {
        PartyEntity entity = jpaRepository.findById(party.getId().asUuid()).orElseGet(PartyEntity::new);
        PartyEntity saved = jpaRepository.save(mapper.toEntity(party, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Party> findById(PartyId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<Party> findByPropertyIdAndEmail(EntityId propertyId, EmailVO email) {
        return jpaRepository.findByPropertyIdAndEmail(propertyId.value(), email.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Party> findByPropertyIdAndPhone(EntityId propertyId, String phone) {
        return jpaRepository.findByPropertyIdAndPhone(propertyId.value(), phone).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPropertyIdAndEmail(EntityId propertyId, EmailVO email) {
        return jpaRepository.existsByPropertyIdAndEmail(propertyId.value(), email.value());
    }

    @Override
    public boolean existsByPropertyIdAndPhone(EntityId propertyId, String phone) {
        return jpaRepository.existsByPropertyIdAndPhone(propertyId.value(), phone);
    }

    @Override
    public Page<Party> findAll(PageRequest pageRequest, PartySearchCriteria criteria) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        Specification<PartyEntity> specification = PartySpecifications.matching(criteria);
        org.springframework.data.domain.Page<PartyEntity> springPage = jpaRepository.findAll(specification, pageable);
        List<Party> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public void deleteById(PartyId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
