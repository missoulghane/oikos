package com.architek.oikos.property.application.usecase;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.BoardMemberView;
import com.architek.oikos.property.application.port.in.ListBoardMembersByPropertyUseCase;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class ListBoardMembersByPropertyService implements ListBoardMembersByPropertyUseCase {

    private final BoardMemberRepository boardMemberRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final AccountLinkingPort accountLinkingPort;

    public ListBoardMembersByPropertyService(BoardMemberRepository boardMemberRepository, PartyDirectoryPort partyDirectoryPort,
                                              AccountLinkingPort accountLinkingPort) {
        this.boardMemberRepository = boardMemberRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.accountLinkingPort = accountLinkingPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardMemberView> listBoardMembers(ListBoardMembersByPropertyQuery query) {
        List<BoardMember> boardMembers = boardMemberRepository.findAllByPropertyId(query.propertyId());

        Set<EntityId> linkedPartyIds = accountLinkingPort.findLinkedPartyIds(
                boardMembers.stream().map(BoardMember::getPartyId).toList());

        return boardMembers.stream()
                .map(boardMember -> {
                    PartyDetails partyDetails = partyDirectoryPort.getPartyById(boardMember.getPartyId());
                    String email = partyDetails.email() != null ? partyDetails.email().value() : null;
                    return BoardMemberView.from(boardMember, partyDetails.fullName(), email,
                            linkedPartyIds.contains(boardMember.getPartyId()));
                })
                .toList();
    }
}
