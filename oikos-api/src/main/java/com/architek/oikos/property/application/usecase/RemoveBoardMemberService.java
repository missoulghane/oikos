package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.RemoveBoardMemberCommand;
import com.architek.oikos.property.application.port.in.RemoveBoardMemberUseCase;
import com.architek.oikos.property.domain.exception.BoardMemberNotFoundException;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;

@Component
public class RemoveBoardMemberService implements RemoveBoardMemberUseCase {

    private final BoardMemberRepository boardMemberRepository;

    public RemoveBoardMemberService(BoardMemberRepository boardMemberRepository) {
        this.boardMemberRepository = boardMemberRepository;
    }

    @Override
    @Transactional
    public void remove(RemoveBoardMemberCommand command) {
        if (boardMemberRepository.findById(command.id()).isEmpty()) {
            throw new BoardMemberNotFoundException(command.id());
        }
        boardMemberRepository.deleteById(command.id());
    }
}
