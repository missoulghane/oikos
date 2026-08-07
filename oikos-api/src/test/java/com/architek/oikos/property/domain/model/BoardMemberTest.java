package com.architek.oikos.property.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class BoardMemberTest {

    @Test
    void create_builds_an_active_board_member_with_no_user_id() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), propertyId, partyId, BoardRole.PRESIDENT);

        assertThat(boardMember.getPropertyId()).isEqualTo(propertyId);
        assertThat(boardMember.getPartyId()).isEqualTo(partyId);
        assertThat(boardMember.getBoardRole()).isEqualTo(BoardRole.PRESIDENT);
        assertThat(boardMember.getStatus()).isEqualTo(BoardMemberStatus.ACTIVE);
        assertThat(boardMember.getUserId()).isNull();
    }

    @Test
    void createPending_builds_a_pending_validation_board_member_with_the_given_user_id() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        EntityId userId = EntityId.newId();
        BoardMember boardMember = BoardMember.createPending(BoardMemberId.newId(), propertyId, partyId, userId, BoardRole.PRESIDENT);

        assertThat(boardMember.getStatus()).isEqualTo(BoardMemberStatus.PENDING_VALIDATION);
        assertThat(boardMember.getUserId()).isEqualTo(userId);
    }

    @Test
    void activate_returns_a_new_active_instance_leaving_the_original_untouched() {
        BoardMember pending = BoardMember.createPending(BoardMemberId.newId(), PropertyId.newId(), EntityId.newId(),
                EntityId.newId(), BoardRole.PRESIDENT);

        BoardMember activated = pending.activate();

        assertThat(pending.getStatus()).isEqualTo(BoardMemberStatus.PENDING_VALIDATION);
        assertThat(activated.getStatus()).isEqualTo(BoardMemberStatus.ACTIVE);
        assertThat(activated.getId()).isEqualTo(pending.getId());
        assertThat(activated.getUserId()).isEqualTo(pending.getUserId());
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        BoardMemberId id = BoardMemberId.newId();
        BoardMember a = BoardMember.create(id, PropertyId.newId(), EntityId.newId(), BoardRole.PRESIDENT);
        BoardMember b = BoardMember.create(id, PropertyId.newId(), EntityId.newId(), BoardRole.TREASURER);

        assertThat(a).isEqualTo(b);
    }
}
