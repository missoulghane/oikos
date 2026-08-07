package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.CreatePendingBoardMemberCommand;
import com.architek.oikos.property.application.port.in.CreatePendingBoardMemberUseCase;
import com.architek.oikos.property.domain.exception.PartyAlreadyHasRoleException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;

/**
 * Called from invitation.infrastructure.adapter.InvitationBoardDirectoryAdapter
 * once a board invitation is accepted. Unlike AddBoardMemberService (direct
 * admin add, immediately ACTIVE), this always creates a PENDING_VALIDATION
 * seat - the party has accepted the invitation link but no role is granted
 * until an admin validates it (see ValidateBoardMemberService).
 */
@Component
public class CreatePendingBoardMemberService implements CreatePendingBoardMemberUseCase {

    private final BoardMemberRepository boardMemberRepository;
    private final PropertyRepository propertyRepository;

    public CreatePendingBoardMemberService(BoardMemberRepository boardMemberRepository, PropertyRepository propertyRepository) {
        this.boardMemberRepository = boardMemberRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public BoardMemberId create(CreatePendingBoardMemberCommand command) {
        propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(command.propertyId()));

        if (boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(
                command.propertyId(), command.partyId(), command.boardRole())) {
            throw new PartyAlreadyHasRoleException();
        }

        BoardMember boardMember = BoardMember.createPending(BoardMemberId.newId(), command.propertyId(),
                command.partyId(), command.userId(), command.boardRole());
        return boardMemberRepository.save(boardMember).getId();
    }
}
