package com.architek.oikos.property.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.property.domain.valueobject.BoardRole;

public interface BoardMemberJpaRepository extends JpaRepository<BoardMemberEntity, UUID> {

    List<BoardMemberEntity> findByPropertyId(UUID propertyId);

    boolean existsByPropertyIdAndPartyIdAndBoardRole(UUID propertyId, UUID partyId, BoardRole boardRole);
}
