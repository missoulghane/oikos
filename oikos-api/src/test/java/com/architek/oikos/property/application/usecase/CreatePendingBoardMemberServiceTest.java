package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.CreatePendingBoardMemberCommand;
import com.architek.oikos.property.domain.exception.PartyAlreadyHasRoleException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CreatePendingBoardMemberServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private PropertyRepository propertyRepository;

    private CreatePendingBoardMemberService newService() {
        return new CreatePendingBoardMemberService(boardMemberRepository, propertyRepository);
    }

    @Test
    void creating_a_pending_board_member_for_an_existing_property_persists_it_as_pending() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        EntityId userId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(propertyId, partyId, BoardRole.PRESIDENT))
                .thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreatePendingBoardMemberCommand(propertyId, partyId, userId, BoardRole.PRESIDENT));

        var captor = org.mockito.ArgumentCaptor.forClass(BoardMember.class);
        org.mockito.Mockito.verify(boardMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BoardMemberStatus.PENDING_VALIDATION);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getPartyId()).isEqualTo(partyId);
        assertThat(captor.getValue().getBoardRole()).isEqualTo(BoardRole.PRESIDENT);
    }

    @Test
    void creating_a_pending_board_member_for_an_unknown_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().create(
                new CreatePendingBoardMemberCommand(propertyId, EntityId.newId(), EntityId.newId(), BoardRole.PRESIDENT)))
                .isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void creating_a_pending_board_member_for_a_party_that_already_holds_the_role_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(propertyId, partyId, BoardRole.PRESIDENT))
                .thenReturn(true);

        assertThatThrownBy(() -> newService().create(
                new CreatePendingBoardMemberCommand(propertyId, partyId, EntityId.newId(), BoardRole.PRESIDENT)))
                .isInstanceOf(PartyAlreadyHasRoleException.class);
    }
}
