package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.invitation.infrastructure.mapper.MembershipRequestPersistenceMapper;
import com.architek.oikos.invitation.infrastructure.persistence.MembershipRequestEntity;
import com.architek.oikos.invitation.infrastructure.persistence.MembershipRequestJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class MembershipRequestRepositoryAdapter implements MembershipRequestRepository {

    private final MembershipRequestJpaRepository jpaRepository;
    private final MembershipRequestPersistenceMapper mapper;

    public MembershipRequestRepositoryAdapter(MembershipRequestJpaRepository jpaRepository, MembershipRequestPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MembershipRequest save(MembershipRequest membershipRequest) {
        MembershipRequestEntity entity = jpaRepository.findById(membershipRequest.getId().asUuid())
                .orElseGet(MembershipRequestEntity::new);
        MembershipRequestEntity saved = jpaRepository.save(mapper.toEntity(membershipRequest, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MembershipRequest> findById(MembershipRequestId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Page<MembershipRequest> findAllByPropertyId(EntityId propertyId, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<MembershipRequestEntity> springPage =
                jpaRepository.findByPropertyId(propertyId.value(), pageable);
        List<MembershipRequest> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public List<MembershipRequest> findAllPendingByUnitId(EntityId unitId) {
        return jpaRepository.findByUnitIdAndStatus(unitId.value(), MembershipRequestStatus.PENDING).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
