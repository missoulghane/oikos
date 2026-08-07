package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.ValidateBoardMemberCommand;
import com.architek.oikos.property.application.port.in.ValidateBoardMemberUseCase;
import com.architek.oikos.property.application.port.out.AccountRoleGrantPort;
import com.architek.oikos.property.domain.exception.BoardMemberNotFoundException;
import com.architek.oikos.property.domain.exception.BoardMemberNotPendingValidationException;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;

@Component
public class ValidateBoardMemberService implements ValidateBoardMemberUseCase {

    private static final String TARGET_ROLE_BOARD_MEMBER = "PROPERTY_BOARD_MEMBER";

    private final BoardMemberRepository boardMemberRepository;
    private final AccountRoleGrantPort accountRoleGrantPort;

    public ValidateBoardMemberService(BoardMemberRepository boardMemberRepository, AccountRoleGrantPort accountRoleGrantPort) {
        this.boardMemberRepository = boardMemberRepository;
        this.accountRoleGrantPort = accountRoleGrantPort;
    }

    @Override
    @Transactional
    public void validate(ValidateBoardMemberCommand command) {
        BoardMember boardMember = boardMemberRepository.findById(command.id())
                .orElseThrow(() -> new BoardMemberNotFoundException(command.id()));

        if (boardMember.getStatus() != BoardMemberStatus.PENDING_VALIDATION) {
            throw new BoardMemberNotPendingValidationException();
        }

        boardMemberRepository.save(boardMember.activate());

        // getUserId() is never null here: only the invitation-acceptance flow
        // (CreatePendingBoardMemberService) creates PENDING_VALIDATION members,
        // always with a userId - a null here would mean that invariant broke
        // elsewhere, so let it fail loudly instead of masking it.
        accountRoleGrantPort.grantPropertyRole(boardMember.getUserId(), boardMember.getPartyId(),
                boardMember.getPropertyId().value(), TARGET_ROLE_BOARD_MEMBER);
    }
}
