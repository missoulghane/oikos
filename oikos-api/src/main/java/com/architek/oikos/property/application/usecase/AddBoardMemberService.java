package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.application.port.in.AddBoardMemberUseCase;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.domain.exception.PartyAlreadyHasRoleException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

@Component
public class AddBoardMemberService implements AddBoardMemberUseCase {

    private final BoardMemberRepository boardMemberRepository;
    private final PropertyRepository propertyRepository;
    private final PartyDirectoryPort partyDirectoryPort;

    public AddBoardMemberService(BoardMemberRepository boardMemberRepository, PropertyRepository propertyRepository,
                                  PartyDirectoryPort partyDirectoryPort) {
        this.boardMemberRepository = boardMemberRepository;
        this.propertyRepository = propertyRepository;
        this.partyDirectoryPort = partyDirectoryPort;
    }

    @Override
    @Transactional
    public BoardMemberId add(AddBoardMemberCommand command) {
        propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(command.propertyId()));

        EntityId partyId = resolveParty(command);

        if (boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(
                command.propertyId(), partyId, command.boardRole())) {
            throw new PartyAlreadyHasRoleException();
        }

        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), command.propertyId(),
                partyId, command.boardRole());
        return boardMemberRepository.save(boardMember).getId();
    }

    /**
     * partyId already existing takes precedence; otherwise a new party is
     * created inline from fullName/email/phone - reusing an existing party by
     * email when one matches, same de-duplication rule as
     * AcceptInvitationService.resolveParty for the owner invite flow.
     */
    private EntityId resolveParty(AddBoardMemberCommand command) {
        if (command.partyId() != null) {
            return command.partyId();
        }
        if (command.fullName() == null || command.fullName().isBlank()) {
            throw new IllegalArgumentException("fullName is required when partyId is not provided");
        }
        EmailVO email = command.email() != null && !command.email().isBlank() ? EmailVO.of(command.email()) : null;
        if (email != null) {
            return partyDirectoryPort.findIdByEmail(email, command.propertyId().value())
                    .orElseGet(() -> partyDirectoryPort.createParty(
                            new PartyDetails(command.fullName(), PartyType.INDIVIDUAL, email, command.phone()),
                            command.propertyId().value()));
        }
        return partyDirectoryPort.createParty(
                new PartyDetails(command.fullName(), PartyType.INDIVIDUAL, null, command.phone()),
                command.propertyId().value());
    }
}
