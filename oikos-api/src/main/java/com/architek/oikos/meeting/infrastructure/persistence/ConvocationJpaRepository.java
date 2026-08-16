package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationJpaRepository extends JpaRepository<ConvocationEntity, UUID> {

    List<ConvocationEntity> findByGeneralMeetingId(UUID generalMeetingId);

    List<ConvocationEntity> findByGeneralMeetingIdAndUnitIdIn(UUID generalMeetingId, Collection<UUID> unitIds);

    List<ConvocationEntity> findByUnitIdIn(Collection<UUID> unitIds);

    Optional<ConvocationEntity> findByConfirmationToken(String confirmationToken);
}
