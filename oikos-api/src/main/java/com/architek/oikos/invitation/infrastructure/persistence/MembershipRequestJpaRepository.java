package com.architek.oikos.invitation.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;

public interface MembershipRequestJpaRepository extends JpaRepository<MembershipRequestEntity, UUID> {

    Page<MembershipRequestEntity> findByPropertyId(UUID propertyId, Pageable pageable);

    List<MembershipRequestEntity> findByUnitIdAndStatus(UUID unitId, MembershipRequestStatus status);
}
