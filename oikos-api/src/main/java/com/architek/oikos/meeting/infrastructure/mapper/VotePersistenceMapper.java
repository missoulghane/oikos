package com.architek.oikos.meeting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.VoteId;
import com.architek.oikos.meeting.infrastructure.persistence.VoteEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface VotePersistenceMapper {

    default VoteEntity toEntity(Vote vote) {
        return toEntity(vote, new VoteEntity());
    }

    default VoteEntity toEntity(Vote vote, VoteEntity entity) {
        entity.setId(vote.getId().asUuid());
        entity.setAgendaItemId(vote.getAgendaItemId().asUuid());
        entity.setUnitId(vote.getUnitId().value());
        entity.setChoice(vote.getChoice());
        entity.setCastAt(vote.getCastAt());
        entity.setCastByUserId(vote.getCastByUserId() != null ? vote.getCastByUserId().value() : null);
        return entity;
    }

    default Vote toDomain(VoteEntity entity) {
        return Vote.reconstruct(VoteId.of(entity.getId()), AgendaItemId.of(entity.getAgendaItemId()),
                EntityId.of(entity.getUnitId()), entity.getChoice(), entity.getCastAt(),
                entity.getCastByUserId() != null ? EntityId.of(entity.getCastByUserId()) : null,
                entity.getCreatedDate());
    }
}
