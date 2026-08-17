package com.architek.oikos.meeting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.ReplyMedium;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.meeting.infrastructure.persistence.ReplyMediumEntity;

@Mapper(componentModel = "spring")
public interface ReplyMediumPersistenceMapper {

    default ReplyMedium toDomain(ReplyMediumEntity entity) {
        return ReplyMedium.of(ReplyMediumCode.of(entity.getCode()), entity.getLabel(), entity.getPosition(),
                entity.isActive());
    }
}
