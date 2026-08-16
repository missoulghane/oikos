package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteJpaRepository extends JpaRepository<VoteEntity, UUID> {

    Optional<VoteEntity> findByAgendaItemIdAndUnitId(UUID agendaItemId, UUID unitId);

    List<VoteEntity> findByAgendaItemId(UUID agendaItemId);
}
