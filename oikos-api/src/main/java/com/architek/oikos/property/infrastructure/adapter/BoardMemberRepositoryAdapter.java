package com.architek.oikos.property.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.infrastructure.mapper.BoardMemberPersistenceMapper;
import com.architek.oikos.property.infrastructure.persistence.BoardMemberEntity;
import com.architek.oikos.property.infrastructure.persistence.BoardMemberJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class BoardMemberRepositoryAdapter implements BoardMemberRepository {

    private final BoardMemberJpaRepository jpaRepository;
    private final BoardMemberPersistenceMapper mapper;

    public BoardMemberRepositoryAdapter(BoardMemberJpaRepository jpaRepository, BoardMemberPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public BoardMember save(BoardMember boardMember) {
        BoardMemberEntity entity = jpaRepository.findById(boardMember.getId().asUuid()).orElseGet(BoardMemberEntity::new);
        BoardMemberEntity saved = jpaRepository.save(mapper.toEntity(boardMember, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BoardMember> findById(BoardMemberId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<BoardMember> findAllByPropertyId(PropertyId propertyId) {
        return jpaRepository.findByPropertyId(propertyId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByPropertyIdAndContactIdAndBoardRole(PropertyId propertyId, EntityId contactId,
                                                                     BoardRole boardRole) {
        return jpaRepository.existsByPropertyIdAndContactIdAndBoardRole(propertyId.asUuid(), contactId.value(), boardRole);
    }

    @Override
    public void deleteById(BoardMemberId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
