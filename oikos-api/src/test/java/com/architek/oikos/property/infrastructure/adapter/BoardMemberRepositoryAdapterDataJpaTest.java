package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.infrastructure.mapper.BoardMemberPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BoardMemberRepositoryAdapter.class, BoardMemberPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class BoardMemberRepositoryAdapterDataJpaTest {

    @Autowired
    private BoardMemberRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_board_member_by_id() {
        PropertyId propertyId = PropertyId.newId();
        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), propertyId, EntityId.newId(), BoardRole.PRESIDENT);

        adapter.save(boardMember);

        assertThat(adapter.findById(boardMember.getId())).isPresent()
                .get().extracting(BoardMember::getBoardRole).isEqualTo(BoardRole.PRESIDENT);
    }

    @Test
    void findAllByPropertyId_returns_only_entries_for_that_property() {
        PropertyId propertyId = PropertyId.newId();
        adapter.save(BoardMember.create(BoardMemberId.newId(), propertyId, EntityId.newId(), BoardRole.PRESIDENT));
        adapter.save(BoardMember.create(BoardMemberId.newId(), PropertyId.newId(), EntityId.newId(), BoardRole.TREASURER));

        assertThat(adapter.findAllByPropertyId(propertyId)).hasSize(1);
    }

    @Test
    void existsByPropertyIdAndPartyIdAndBoardRole_reflects_persisted_state() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        assertThat(adapter.existsByPropertyIdAndPartyIdAndBoardRole(propertyId, partyId, BoardRole.PRESIDENT))
                .isFalse();

        adapter.save(BoardMember.create(BoardMemberId.newId(), propertyId, partyId, BoardRole.PRESIDENT));

        assertThat(adapter.existsByPropertyIdAndPartyIdAndBoardRole(propertyId, partyId, BoardRole.PRESIDENT))
                .isTrue();
        assertThat(adapter.existsByPropertyIdAndPartyIdAndBoardRole(propertyId, partyId, BoardRole.TREASURER))
                .isFalse();
    }

    @Test
    void deleteById_removes_the_entry() {
        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), PropertyId.newId(), EntityId.newId(),
                BoardRole.MEMBER);
        adapter.save(boardMember);

        adapter.deleteById(boardMember.getId());

        assertThat(adapter.findById(boardMember.getId())).isEmpty();
    }
}
