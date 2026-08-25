package com.architek.oikos.property.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.application.port.in.AddBoardMemberUseCase;
import com.architek.oikos.property.application.port.out.AccountDirectoryPort;
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
    private final AccountDirectoryPort accountDirectoryPort;

    public AddBoardMemberService(BoardMemberRepository boardMemberRepository, PropertyRepository propertyRepository,
                                  PartyDirectoryPort partyDirectoryPort, AccountDirectoryPort accountDirectoryPort) {
        this.boardMemberRepository = boardMemberRepository;
        this.propertyRepository = propertyRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
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
     * partyId already existing takes precedence; otherwise the party is resolved
     * from the coordinates given - email de fiche, puis téléphone, puis email de
     * compte - et créée seulement quand aucun ne correspond. Same chain as
     * AddUnitOwnerService: a syndic often knows one coordinate without the
     * other, and matching on email alone created a second fiche for someone
     * already on file, paid for later in duplicate convocations and dues calls.
     *
     * <p>Le troisième filet vise le cas le plus trompeur : le syndic tape
     * l'adresse avec laquelle la personne se connecte, qui n'est pas celle
     * inscrite sur sa fiche. Rien ne correspondait, et un second contact
     * naissait pour quelqu'un qui possède déjà un lot dans l'immeuble - avec le
     * nom, parfois approximatif, saisi à ce moment-là.
     *
     * <p>Both coordinates may be absent: a conseil syndical member with neither
     * is recorded on their name alone. Nothing can be de-duplicated then, and
     * nothing can be sent to them either - which is exactly what was asked for.
     */
    private EntityId resolveParty(AddBoardMemberCommand command) {
        if (command.partyId() != null) {
            return command.partyId();
        }
        if (command.fullName() == null || command.fullName().isBlank()) {
            throw new IllegalArgumentException("fullName is required when partyId is not provided");
        }
        EntityId propertyId = command.propertyId().value();
        EmailVO email = command.email() != null && !command.email().isBlank() ? EmailVO.of(command.email()) : null;
        Optional<EntityId> byEmail = email != null
                ? partyDirectoryPort.findIdByEmail(email, propertyId)
                : Optional.empty();
        return byEmail
                .or(() -> partyDirectoryPort.findIdByPhone(command.phone(), propertyId))
                .or(() -> email != null ? accountDirectoryPort.findLinkedPartyInProperty(email, propertyId) : Optional.empty())
                .orElseGet(() -> partyDirectoryPort.createParty(
                        new PartyDetails(command.fullName(), PartyType.INDIVIDUAL, email, command.phone()),
                        propertyId));
    }
}
