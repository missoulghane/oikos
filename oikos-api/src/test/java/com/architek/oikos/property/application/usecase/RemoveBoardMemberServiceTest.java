package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.RemoveBoardMemberCommand;
import com.architek.oikos.property.domain.exception.BoardMemberNotFoundException;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RemoveBoardMemberServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Test
    void removing_an_existing_entry_deletes_it() {
        BoardMemberId id = BoardMemberId.newId();
        BoardMember boardMember = BoardMember.create(id, PropertyId.newId(), EntityId.newId(), BoardRole.PRESIDENT);
        when(boardMemberRepository.findById(id)).thenReturn(Optional.of(boardMember));

        new RemoveBoardMemberService(boardMemberRepository).remove(new RemoveBoardMemberCommand(id));

        verify(boardMemberRepository).deleteById(id);
    }

    @Test
    void removing_an_unknown_entry_throws() {
        BoardMemberId id = BoardMemberId.newId();
        when(boardMemberRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RemoveBoardMemberService(boardMemberRepository).remove(new RemoveBoardMemberCommand(id)))
                .isInstanceOf(BoardMemberNotFoundException.class);
    }
}
