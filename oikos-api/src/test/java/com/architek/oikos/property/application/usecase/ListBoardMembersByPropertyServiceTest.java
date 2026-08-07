package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

@ExtendWith(MockitoExtension.class)
class ListBoardMembersByPropertyServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    @Mock
    private AccountLinkingPort accountLinkingPort;

    private ListBoardMembersByPropertyService newService() {
        return new ListBoardMembersByPropertyService(boardMemberRepository, partyDirectoryPort, accountLinkingPort);
    }

    @Test
    void listing_membres_syndic_maps_the_repository_entries_to_views() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), propertyId, partyId, BoardRole.PRESIDENT);
        when(boardMemberRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(boardMember));
        when(partyDirectoryPort.getPartyById(partyId))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null));
        when(accountLinkingPort.findLinkedPartyIds(anyList())).thenReturn(Set.of(partyId));

        var views = newService().listBoardMembers(new ListBoardMembersByPropertyQuery(propertyId));

        assertThat(views).extracting(view -> view.boardRole()).containsExactly(BoardRole.PRESIDENT);
        assertThat(views).extracting(view -> view.partyFullName()).containsExactly("Jane Doe");
        assertThat(views).extracting(view -> view.partyEmail()).containsExactly("jane.doe@example.com");
        assertThat(views).extracting(view -> view.hasLinkedAccount()).containsExactly(true);
    }

    @Test
    void listing_membres_syndic_reports_no_linked_account_when_the_party_id_is_absent_from_the_linked_set() {
        PropertyId propertyId = PropertyId.newId();
        EntityId partyId = EntityId.newId();
        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), propertyId, partyId, BoardRole.TREASURER);
        when(boardMemberRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(boardMember));
        when(partyDirectoryPort.getPartyById(partyId))
                .thenReturn(new PartyDetails("John Doe", PartyType.INDIVIDUAL, EmailVO.of("john.doe@example.com"), null));
        when(accountLinkingPort.findLinkedPartyIds(anyList())).thenReturn(Set.of());

        var views = newService().listBoardMembers(new ListBoardMembersByPropertyQuery(propertyId));

        assertThat(views).extracting(view -> view.hasLinkedAccount()).containsExactly(false);
    }
}
