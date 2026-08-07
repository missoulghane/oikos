package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.ValidateBoardMemberCommand;
import com.architek.oikos.property.application.port.out.AccountRoleGrantPort;
import com.architek.oikos.property.domain.exception.BoardMemberNotFoundException;
import com.architek.oikos.property.domain.exception.BoardMemberNotPendingValidationException;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ValidateBoardMemberServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private AccountRoleGrantPort accountRoleGrantPort;

    private ValidateBoardMemberService newService() {
        return new ValidateBoardMemberService(boardMemberRepository, accountRoleGrantPort);
    }

    @Test
    void validating_a_pending_board_member_activates_it_and_grants_the_role() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        EntityId userId = EntityId.newId();
        BoardMember pending = BoardMember.createPending(BoardMemberId.newId(), propertyId, partyId, userId, BoardRole.PRESIDENT);
        when(boardMemberRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().validate(new ValidateBoardMemberCommand(pending.getId()));

        ArgumentCaptor<BoardMember> captor = ArgumentCaptor.forClass(BoardMember.class);
        verify(boardMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BoardMemberStatus.ACTIVE);
        verify(accountRoleGrantPort).grantPropertyRole(userId, partyId, propertyId.value(), "PROPERTY_BOARD_MEMBER");
    }

    @Test
    void validating_an_already_active_board_member_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        BoardMember active = BoardMember.create(BoardMemberId.newId(), propertyId, EntityId.newId(), BoardRole.PRESIDENT);
        when(boardMemberRepository.findById(active.getId())).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> newService().validate(new ValidateBoardMemberCommand(active.getId())))
                .isInstanceOf(BoardMemberNotPendingValidationException.class);
        verify(boardMemberRepository, never()).save(any());
        verify(accountRoleGrantPort, never()).grantPropertyRole(any(), any(), any(), any());
    }

    @Test
    void validating_an_unknown_board_member_is_rejected() {
        BoardMemberId id = BoardMemberId.newId();
        when(boardMemberRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().validate(new ValidateBoardMemberCommand(id)))
                .isInstanceOf(BoardMemberNotFoundException.class);
    }
}
