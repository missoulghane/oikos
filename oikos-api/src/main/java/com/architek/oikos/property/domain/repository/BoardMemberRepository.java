package com.architek.oikos.property.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface BoardMemberRepository {

    BoardMember save(BoardMember boardMember);

    Optional<BoardMember> findById(BoardMemberId id);

    List<BoardMember> findAllByPropertyId(PropertyId propertyId);

    boolean existsByPropertyIdAndPartyIdAndBoardRole(PropertyId propertyId, EntityId partyId, BoardRole boardRole);

    void deleteById(BoardMemberId id);
}
