package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class BoardMemberTest {

    @Test
    void create_builds_a_board_member_with_the_given_fields() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), propertyId, partyId, BoardRole.PRESIDENT);

        assertThat(boardMember.getPropertyId()).isEqualTo(propertyId);
        assertThat(boardMember.getPartyId()).isEqualTo(partyId);
        assertThat(boardMember.getBoardRole()).isEqualTo(BoardRole.PRESIDENT);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        BoardMemberId id = BoardMemberId.newId();
        BoardMember a = BoardMember.create(id, PropertyId.newId(), EntityId.newId(), BoardRole.PRESIDENT);
        BoardMember b = BoardMember.create(id, PropertyId.newId(), EntityId.newId(), BoardRole.TREASURER);

        assertThat(a).isEqualTo(b);
    }
}
