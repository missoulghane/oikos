package com.architek.oikos.meeting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.ConvocationChannel;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationChannelEntity;

@Mapper(componentModel = "spring")
public interface ConvocationChannelPersistenceMapper {

    default ConvocationChannel toDomain(ConvocationChannelEntity entity) {
        return ConvocationChannel.of(ChannelCode.of(entity.getCode()), entity.getLabel(), entity.isAutomated(),
                entity.getPosition(), entity.isActive());
    }
}
