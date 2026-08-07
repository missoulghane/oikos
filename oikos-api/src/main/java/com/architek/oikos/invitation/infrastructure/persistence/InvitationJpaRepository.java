package com.architek.oikos.invitation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;

public interface InvitationJpaRepository extends JpaRepository<InvitationEntity, UUID> {

    Optional<InvitationEntity> findByToken(String token);

    Page<InvitationEntity> findByPropertyId(UUID propertyId, Pageable pageable);

    List<InvitationEntity> findByPropertyIdAndTypeAndStatus(UUID propertyId, InvitationType type, InvitationStatus status);
}
