package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.dto.RecipientCandidateView;
import com.architek.oikos.messaging.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberInfo;
import com.architek.oikos.messaging.application.query.ListRecipientCandidatesQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListRecipientCandidatesServiceTest {

    @Mock
    private PropertyMemberDirectoryPort propertyMemberDirectoryPort;

    @Mock
    private PartyAccountDirectoryPort partyAccountDirectoryPort;

    private ListRecipientCandidatesService newService() {
        return new ListRecipientCandidatesService(propertyMemberDirectoryPort, partyAccountDirectoryPort);
    }

    @Test
    void excludes_the_caller_members_without_a_linked_account_and_applies_search() {
        EntityId propertyId = EntityId.newId();
        EntityId caller = EntityId.newId();
        EntityId callerParty = EntityId.newId();
        EntityId aliceParty = EntityId.newId();
        EntityId aliceUser = EntityId.newId();
        EntityId bobParty = EntityId.newId();

        PropertyMemberInfo callerInfo = new PropertyMemberInfo(callerParty, "Caller Self", "Copropriétaire", true, List.of("A1"), false);
        PropertyMemberInfo aliceInfo = new PropertyMemberInfo(aliceParty, "Alice Owner", "Copropriétaire", true, List.of("B2"), false);
        PropertyMemberInfo bobInfo = new PropertyMemberInfo(bobParty, "Bob NoAccount", "Copropriétaire", false, List.of("C3"), false);

        when(propertyMemberDirectoryPort.listMembers(propertyId)).thenReturn(List.of(callerInfo, aliceInfo, bobInfo));
        when(partyAccountDirectoryPort.resolveUserIds(List.of(callerParty, aliceParty)))
                .thenReturn(Map.of(callerParty, caller, aliceParty, aliceUser));

        List<RecipientCandidateView> candidates = newService().listCandidates(
                new ListRecipientCandidatesQuery(propertyId, caller, null));

        assertThat(candidates).extracting(RecipientCandidateView::fullName).containsExactly("Alice Owner");
    }

    @Test
    void filters_candidates_by_search_text_case_insensitively() {
        EntityId propertyId = EntityId.newId();
        EntityId caller = EntityId.newId();
        EntityId aliceParty = EntityId.newId();
        EntityId aliceUser = EntityId.newId();
        EntityId bobParty = EntityId.newId();
        EntityId bobUser = EntityId.newId();

        PropertyMemberInfo aliceInfo = new PropertyMemberInfo(aliceParty, "Alice Owner", "Copropriétaire", true, List.of("A1"), false);
        PropertyMemberInfo bobInfo = new PropertyMemberInfo(bobParty, "Bob Board", "Bureau de syndic", true, List.of(), true);

        when(propertyMemberDirectoryPort.listMembers(propertyId)).thenReturn(List.of(aliceInfo, bobInfo));
        when(partyAccountDirectoryPort.resolveUserIds(List.of(aliceParty, bobParty)))
                .thenReturn(Map.of(aliceParty, aliceUser, bobParty, bobUser));

        List<RecipientCandidateView> candidates = newService().listCandidates(
                new ListRecipientCandidatesQuery(propertyId, caller, "board"));

        assertThat(candidates).extracting(RecipientCandidateView::fullName).containsExactly("Bob Board");
    }
}
