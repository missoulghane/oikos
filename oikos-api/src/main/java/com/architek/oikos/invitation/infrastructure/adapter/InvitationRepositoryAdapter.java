package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.invitation.infrastructure.mapper.InvitationPersistenceMapper;
import com.architek.oikos.invitation.infrastructure.persistence.InvitationEntity;
import com.architek.oikos.invitation.infrastructure.persistence.InvitationJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class InvitationRepositoryAdapter implements InvitationRepository {

    private final InvitationJpaRepository jpaRepository;
    private final InvitationPersistenceMapper mapper;

    public InvitationRepositoryAdapter(InvitationJpaRepository jpaRepository, InvitationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Invitation save(Invitation invitation) {
        InvitationEntity entity = jpaRepository.findById(invitation.getId().asUuid()).orElseGet(InvitationEntity::new);
        InvitationEntity saved = jpaRepository.save(mapper.toEntity(invitation, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Invitation> findById(InvitationId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public Page<Invitation> findAllByPropertyId(EntityId propertyId, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<InvitationEntity> springPage =
                jpaRepository.findByPropertyId(propertyId.value(), pageable);
        List<Invitation> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public Optional<Invitation> findPublicByPropertyId(EntityId propertyId) {
        return jpaRepository.findByPropertyIdAndTypeOrderByCreatedDateAsc(propertyId.value(), InvitationType.PUBLIC).stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Invitation> findOutstandingPrivateByPartyId(EntityId partyId) {
        return jpaRepository.findByTargetPartyIdAndStatusOrderByCreatedDateDesc(partyId.value(), InvitationStatus.ACTIVE)
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public List<Invitation> findAllByPropertyIdAndTypeAndStatus(EntityId propertyId, InvitationType type, InvitationStatus status) {
        return jpaRepository.findByPropertyIdAndTypeAndStatus(propertyId.value(), type, status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
