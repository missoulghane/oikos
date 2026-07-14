package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.infrastructure.persistence.BoardMemberEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface BoardMemberPersistenceMapper {

    default BoardMemberEntity toEntity(BoardMember boardMember) {
        return toEntity(boardMember, new BoardMemberEntity());
    }

    default BoardMemberEntity toEntity(BoardMember boardMember, BoardMemberEntity entity) {
        entity.setId(boardMember.getId().asUuid());
        entity.setPropertyId(boardMember.getPropertyId().asUuid());
        entity.setContactId(boardMember.getContactId().value());
        entity.setBoardRole(boardMember.getBoardRole());
        return entity;
    }

    default BoardMember toDomain(BoardMemberEntity entity) {
        return BoardMember.reconstruct(BoardMemberId.of(entity.getId()), PropertyId.of(entity.getPropertyId()),
                EntityId.of(entity.getContactId()), entity.getBoardRole());
    }
}
