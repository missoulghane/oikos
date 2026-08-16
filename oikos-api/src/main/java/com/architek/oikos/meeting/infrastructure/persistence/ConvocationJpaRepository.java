package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConvocationJpaRepository extends JpaRepository<ConvocationEntity, UUID> {

    List<ConvocationEntity> findByGeneralMeetingId(UUID generalMeetingId);

    List<ConvocationEntity> findByGeneralMeetingIdAndUnitIdIn(UUID generalMeetingId, Collection<UUID> unitIds);

    List<ConvocationEntity> findByUnitIdIn(Collection<UUID> unitIds);

    Optional<ConvocationEntity> findByConfirmationToken(String confirmationToken);

    Optional<ConvocationEntity> findByGeneralMeetingIdAndConfirmationCode(UUID generalMeetingId,
                                                                          String confirmationCode);

    /** Every code already used in one meeting - what generation checks against. */
    @Query("select c.confirmationCode from ConvocationEntity c where c.generalMeetingId = :generalMeetingId")
    List<String> findConfirmationCodesByGeneralMeetingId(@Param("generalMeetingId") UUID generalMeetingId);
}
