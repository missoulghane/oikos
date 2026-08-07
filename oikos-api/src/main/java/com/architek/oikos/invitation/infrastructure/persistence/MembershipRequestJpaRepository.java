package com.architek.oikos.invitation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;

public interface MembershipRequestJpaRepository extends JpaRepository<MembershipRequestEntity, UUID> {

    Page<MembershipRequestEntity> findByPropertyId(UUID propertyId, Pageable pageable);

    List<MembershipRequestEntity> findByPropertyId(UUID propertyId);

    List<MembershipRequestEntity> findByUnitIdAndStatus(UUID unitId, MembershipRequestStatus status);

    List<MembershipRequestEntity> findByUserId(UUID userId);

    Optional<MembershipRequestEntity> findByInvitationIdAndUnitIdAndUserId(UUID invitationId, UUID unitId, UUID userId);
}
