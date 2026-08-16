package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.repository.VoteRepository;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.infrastructure.mapper.VotePersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.VoteEntity;
import com.architek.oikos.meeting.infrastructure.persistence.VoteJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class VoteRepositoryAdapter implements VoteRepository {

    private final VoteJpaRepository jpaRepository;
    private final VotePersistenceMapper mapper;

    public VoteRepositoryAdapter(VoteJpaRepository jpaRepository, VotePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Vote save(Vote vote) {
        return mapper.toDomain(jpaRepository.save(toManagedEntity(vote)));
    }

    @Override
    public List<Vote> saveAll(List<Vote> votes) {
        List<VoteEntity> entities = votes.stream().map(this::toManagedEntity).toList();
        return jpaRepository.saveAll(entities).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Vote> findByAgendaItemIdAndUnitId(AgendaItemId agendaItemId, EntityId unitId) {
        return jpaRepository.findByAgendaItemIdAndUnitId(agendaItemId.asUuid(), unitId.value()).map(mapper::toDomain);
    }

    @Override
    public List<Vote> findByAgendaItemId(AgendaItemId agendaItemId) {
        return jpaRepository.findByAgendaItemId(agendaItemId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    private VoteEntity toManagedEntity(Vote vote) {
        VoteEntity entity = jpaRepository.findById(vote.getId().asUuid()).orElseGet(VoteEntity::new);
        return mapper.toEntity(vote, entity);
    }
}
