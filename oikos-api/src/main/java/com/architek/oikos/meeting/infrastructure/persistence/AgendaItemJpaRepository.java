package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgendaItemJpaRepository extends JpaRepository<AgendaItemEntity, UUID> {

    List<AgendaItemEntity> findByGeneralMeetingIdOrderByPositionAsc(UUID generalMeetingId);

    long countByGeneralMeetingId(UUID generalMeetingId);

    @Query("select max(a.position) from AgendaItemEntity a where a.generalMeetingId = :generalMeetingId")
    Optional<Integer> findMaxPosition(@Param("generalMeetingId") UUID generalMeetingId);
}
