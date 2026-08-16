package com.architek.oikos.meeting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.infrastructure.persistence.AgendaItemEntity;

@Mapper(componentModel = "spring")
public interface AgendaItemPersistenceMapper {

    default AgendaItemEntity toEntity(AgendaItem item) {
        return toEntity(item, new AgendaItemEntity());
    }

    default AgendaItemEntity toEntity(AgendaItem item, AgendaItemEntity entity) {
        entity.setId(item.getId().asUuid());
        entity.setGeneralMeetingId(item.getGeneralMeetingId().asUuid());
        entity.setLabel(item.getLabel());
        entity.setDescription(item.getDescription());
        entity.setPosition(item.getPosition());
        entity.setMajorityRule(item.getMajorityRule());
        entity.setVoteSessionStatus(item.getVoteSessionStatus());
        return entity;
    }

    default AgendaItem toDomain(AgendaItemEntity entity) {
        return AgendaItem.reconstruct(AgendaItemId.of(entity.getId()),
                GeneralMeetingId.of(entity.getGeneralMeetingId()), entity.getLabel(), entity.getDescription(),
                entity.getPosition(), entity.getMajorityRule(), entity.getVoteSessionStatus(), entity.getCreatedDate());
    }
}
