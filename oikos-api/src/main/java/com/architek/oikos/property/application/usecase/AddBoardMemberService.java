package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.application.port.in.AddBoardMemberUseCase;
import com.architek.oikos.property.domain.exception.ContactAlreadyHasRoleException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;

@Component
public class AddBoardMemberService implements AddBoardMemberUseCase {

    private final BoardMemberRepository boardMemberRepository;
    private final PropertyRepository propertyRepository;

    public AddBoardMemberService(BoardMemberRepository boardMemberRepository, PropertyRepository propertyRepository) {
        this.boardMemberRepository = boardMemberRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public BoardMemberId add(AddBoardMemberCommand command) {
        propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(command.propertyId()));

        if (boardMemberRepository.existsByPropertyIdAndContactIdAndBoardRole(
                command.propertyId(), command.contactId(), command.boardRole())) {
            throw new ContactAlreadyHasRoleException();
        }

        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), command.propertyId(),
                command.contactId(), command.boardRole());
        return boardMemberRepository.save(boardMember).getId();
    }
}
